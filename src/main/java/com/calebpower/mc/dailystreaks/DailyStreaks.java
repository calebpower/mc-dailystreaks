package com.calebpower.mc.dailystreaks;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.calebpower.mc.dailystreaks.command.Subcommand;
import com.calebpower.mc.dailystreaks.db.Database;
import com.calebpower.mc.dailystreaks.model.ValidMaterial;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DailyStreaks extends JavaPlugin {

  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  private Database db = null;
  private Map<Material, Integer> currentMaterials = new LinkedHashMap<>();
  private OkHttpClient http = null;
  private Set<Subcommand> commands = new TreeSet<>();

  @Override public void onEnable() {
    getLogger().log(Level.INFO, "Engineered by LordInateur");
    http = new OkHttpClient();
    db = new Database();
    
    try {
      db.load();
      
      String[] matNames = new String[3];
      int[] matQuantities = new int[3];
      
      boolean goodQueries = true;
      
      for(int i = 0; i < 3; i++) {
        matNames[i] = db.getConfig(
            String.format("item_%1$d_mat", i));
        
        try {
          matQuantities[i] = Integer.parseInt(
              db.getConfig(
                  String.format("item_%1$d_qty", i)));
        } catch(NumberFormatException e) { }
        
        if(null == matNames[i] || 0 == matQuantities[i]) {
          goodQueries = false;
          refreshMaterials();
          break;
        }
      }
      
      if(goodQueries) {
        currentMaterials.clear();
        for(int i = 0; i < 3; i++)
          currentMaterials.put(
              Material.valueOf(matNames[i]),
              matQuantities[i]);
      }
      
    } catch(NoSuchAlgorithmException | SQLException e) {
      getLogger().log(Level.SEVERE, e.getMessage());
      Bukkit.getPluginManager().disablePlugin(this);
    }

    // instantiate subcommands

  }

  @Override public void onDisable() {
    db = null;
    http = null;
    getLogger().log(Level.INFO, "So long, and thanks for all the fish!");
  }

  public Database getDB() {
    return db;
  }

  public Map<Material, Integer> getCurrentMaterials() {
    return currentMaterials;
  }

  public Set<Subcommand> getSubcommands() {
    return Collections.unmodifiableSet(commands);
  }

  public void refreshMaterials() throws NoSuchAlgorithmException, SQLException {
    currentMaterials.clear();
    
    for(int i = 0; i < 3; i++) {
      Material material = null;
      do {
        material = Material
            .valueOf(
                ValidMaterial.values()[
                    SecureRandom.getInstanceStrong().nextInt(
                        ValidMaterial.values().length - 1) + 1]
                .name());
      } while(currentMaterials.keySet().contains(material));

      currentMaterials.put(
          material,
          SecureRandom.getInstanceStrong().nextInt(
              material.getMaxStackSize()));

      db.setConfig(
          String.format("item_%1$d_mat", i),
          material.name());

      db.setConfig(
          String.format("item_%1$d_qty", i),
          String.valueOf(currentMaterials.get(material)));
    }
  }

  public void publishMessage(String message) throws IOException, SQLException {
    String url = db.getConfig("discord_webhook");
    if(null == url) return;
    
    RequestBody body = RequestBody.create(
        new JSONObject().put("content", message).toString(),
        JSON);
    Request req = new Request.Builder()
      .url(url)
      .post(body)
      .build();

    try(Response res = http.newCall(req).execute()) { }
  }
  
}
