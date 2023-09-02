package com.calebpower.mc.dailystreaks.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.calebpower.mc.dailystreaks.db.SQLBuilder.Comparison;
import com.calebpower.mc.dailystreaks.model.Denizen;
import com.calebpower.mc.dailystreaks.model.QuestSlot;
import com.calebpower.mc.mcdb.McDbApi;

public class Database {

  private final String dbHandle = "game";
  private final String dbPrefix = "streak_";
  
  private McDbApi dbAPI = null;
  
  public void load() throws SQLException {
    if(null == (dbAPI = McDbApi.getInstance()))
      throw new SQLException("could not load McDbApi");
    
    Connection con = null;
    PreparedStatement stmt = null;
    
    try {
      con = dbAPI.connect(dbHandle);
      String dbName = con.getCatalog();
      Set<String> fileList = new TreeSet<>();
      
      CodeSource src = Database.class.getProtectionDomain().getCodeSource();
      if(null != src) {
        URL jar = src.getLocation();
        try(ZipInputStream zip = new ZipInputStream(jar.openStream())) {
          ZipEntry entry = null;
          while(null != (entry = zip.getNextEntry())) {
            var file = entry.getName();
            if(file.matches("db/.*\\.sql"))
              fileList.add(file);
          }
        } catch(IOException e) {
          e.printStackTrace();
        }
      }
      
      for(var file : fileList) {
        String resource = null;
        
        try(
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(file),
                    StandardCharsets.UTF_8))) {
          StringBuilder resBuilder = new StringBuilder();
          for(String line; null != (line = reader.readLine()); resBuilder.append(line.trim()).append(' '));
          resource = resBuilder.deleteCharAt(resBuilder.length() - 1).toString();
          stmt = con.prepareStatement(
              resource.replace("${database}", dbName).replace("${prefix}", dbPrefix));
          stmt.execute();
        } catch(IOException e) {
          if(null == resource)
            throw new SQLException(
                "Database bootstrap scripts could not be read.");
        } finally {
          close(null, stmt, null);
        }
      }
    } finally {
      close(con, null, null);
    }
  }

  // get denizens, both with and without an existing streak, that completed all quests
  public Set<Denizen> getDenizensWithCompletedQuests() throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "denizen",
            "id",
            "ign",
            "quests",
            "streak",
            "last_update")
        .where(
            "quests",
            Comparison.GREATER_THAN_OR_EQUAL_TO)
        .toString());
    stmt.setByte(1, (byte)0x7);
    ResultSet res = stmt.executeQuery();

    Set<Denizen> denizens = new HashSet<>();
    while(res.next())
      denizens.add(
          new Denizen(
              SQLBuilder.bytesToUUID(
                  res.getBytes("id")),
              res.getString("ign"),
              res.getInt("streak"),
              res.getByte("quests"),
              res.getTimestamp("last_update")));

    close(con, stmt, res);
    return denizens;
  }

  // get all denizens with either (a) streaks, but incomplete quests or (b) partially complete quests
  public Set<Denizen> getDenizensWithIncompleteQuests() throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "denizen",
            "id",
            "ign",
            "quests",
            "streak",
            "last_update")
        .where(
            "quests",
            Comparison.LESS_THAN)
        .where(
            "quests",
            Comparison.GREATER_THAN)
        .or()
        .where(
            "streak",
            Comparison.GREATER_THAN)
        .toString());
    stmt.setByte(1, (byte)0x7);
    stmt.setByte(2, (byte)0x0);
    stmt.setInt(3, 0);
    ResultSet res = stmt.executeQuery();

    Set<Denizen> denizens = new HashSet<>();
    while(res.next())
      denizens.add(
          new Denizen(
              SQLBuilder.bytesToUUID(
                  res.getBytes("id")),
              res.getString("ign"),
              res.getInt("streak"),
              res.getByte("quests"),
              res.getTimestamp("last_update")));

    close(con, stmt, res);
    return denizens;
  }
  
  public Denizen getDenizen(UUID id) throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "denizen",
            "ign",
            "quests",
            "streak",
            "last_update")
        .where("id")
        .limit(1)
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    ResultSet res = stmt.executeQuery();

    Denizen denizen = null;
    if(res.next())
      denizen = new Denizen(
          id,
          res.getString("ign"),
          res.getInt("streak"),
          res.getByte("quests"),
          res.getTimestamp("last_update"));

    close(con, stmt, res);
    return denizen;
  }

  public void setDenizen(Denizen denizen) throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().update(
            dbPrefix + "denizen",
            "ign",
            "quests",
            "streak")
        .where("id")
        .toString());
    stmt.setString(1, denizen.getIGN());
    stmt.setByte(2, QuestSlot.toRaw(denizen.getQuestSlots()));
    stmt.setInt(3, denizen.getStreak());
    stmt.setBytes(4, SQLBuilder.uuidToBytes(denizen.getID()));

    if(0 >= stmt.executeUpdate()) {
      close(null, stmt, null);
      stmt = con.prepareStatement(
          new SQLBuilder().insert(
              dbPrefix + "denizen",
              "ign",
              "quests",
              "streak",
              "id")
          .toString());
      stmt.setString(1, denizen.getIGN());
      stmt.setByte(2, QuestSlot.toRaw(denizen.getQuestSlots()));
      stmt.setInt(3, denizen.getStreak());
      stmt.setBytes(4, SQLBuilder.uuidToBytes(denizen.getID()));
      stmt.executeUpdate();
    }

    close(null, stmt, null);
    stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "denizen",
            "last_update")
        .where("id")
        .limit(1)
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(denizen.getID()));
    ResultSet res = stmt.executeQuery();

    if(res.next())
      denizen.setLastSaveTime(
          res.getTimestamp("last_update"));

    close(con, stmt, res);
  }

  public String getConfig(String key) throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "cfgstate",
            "cfg_val")
        .where("cfg_key")
        .limit(1)
        .toString());
    stmt.setString(1, key);
    ResultSet res = stmt.executeQuery();

    String val = res.next() ? res.getString("cfg_val") : null;
    close(con, stmt, res);
    return val;
  }

  public void setConfig(String key, String val) throws SQLException {
    Connection con = dbAPI.connect(dbHandle);
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().replace(
            dbPrefix + "cfgstate",
            "cfg_key",
            "cfg_val")
        .toString());
    stmt.setString(1, key);
    stmt.setString(2, val);
    stmt.executeUpdate();
    close(con, stmt, null);
  }

  private void close(Connection con, PreparedStatement stmt, ResultSet res) {
    if(null != res)
      try {
        res.close();
      } catch(SQLException e) { }

    if(null != stmt)
      try {
        stmt.close();
      } catch(SQLException e) { }

    if(null != con)
      try {
        con.close();
      } catch(SQLException e) { }
  }
  
}
