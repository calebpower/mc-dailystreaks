package com.calebpower.mc.dailystreaks;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;

import com.calebpower.mc.dailystreaks.command.ConfigSubcommand;
import com.calebpower.mc.dailystreaks.command.HelpSubcommand;
import com.calebpower.mc.dailystreaks.command.RefreshSubcommand;
import com.calebpower.mc.dailystreaks.command.Subcommand;
import com.calebpower.mc.dailystreaks.command.UISubcommand;
import com.calebpower.mc.dailystreaks.db.Database;
import com.calebpower.mc.dailystreaks.model.ValidMaterial;
import com.calebpower.mc.dailystreaks.shop.StreakTracker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DailyStreaks extends JavaPlugin {

  private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  
  private Database db = null;
  private Map<Material, Integer> currentMaterials = new LinkedHashMap<>();
  private Map<String, String> config = new TreeMap<>();
  private OkHttpClient http = null;
  private Set<Subcommand> commands = new TreeSet<>();
  private StreakTracker tracker = null;

  @Override public void onEnable() {
    getLogger().log(Level.INFO, "Engineered by LordInateur");
    http = new OkHttpClient();
    db = new Database();
    tracker = new StreakTracker(this);
    
    try {
      db.load();

      String prefix = db.getConfig("msg_prefix");
      config.put("msg_prefix", null == prefix ? "&d[&7DailyStreak&d] &c" : prefix);
      config.put("discord_webhook", db.getConfig("discord_webhook"));
      config.put("bc_command", db.getConfig("bc_command"));
      
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

      tracker.start();
      
    } catch(NoSuchAlgorithmException | SQLException e) {
      getLogger().log(Level.SEVERE, e.getMessage());
      Bukkit.getPluginManager().disablePlugin(this);
    }

    // instantiate subcommands
    commands.add(new ConfigSubcommand(this));
    commands.add(new HelpSubcommand(this));
    commands.add(new RefreshSubcommand(this));
    commands.add(new UISubcommand(this));
  }

  @Override public void onDisable() {
    tracker.stop();
    tracker = null;
    db = null;
    http = null;
    getLogger().log(Level.INFO, "So long, and thanks for all the fish!");
  }

  public String getConfig(String key) {
    return config.get(key);
  }

  public void setConfig(String key, String val) {
    config.put(key, val);
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

  public void message(CommandSender sender, String message) {
    message(sender, message, null);
  }

  public void message(CommandSender sender, String message, String hover) {
    message(sender, message, hover, new Object[0]);        
  }

  public void message(CommandSender sender, String message, String hover, Object... args) {
    TextComponent component = new TextComponent(
        ChatColor.translateAlternateColorCodes(
            '&',
            config.get("msg_prefix") + String.format(message, args)));

    if(null == hover)
      component.setHoverEvent(
          new HoverEvent(
              HoverEvent.Action.SHOW_TEXT,
              new Text(
                  ChatColor.translateAlternateColorCodes(
                      '&',
                      "&r&b" + hover))));

    sender.spigot().sendMessage(component);
  }

  public void broadcast(String message) throws IOException, SQLException {
    String bcCmd = db.getConfig("bc_command");
    if(null != bcCmd) {
      Bukkit.getServer().dispatchCommand(
          Bukkit.getConsoleSender(),
          bcCmd.replace("[[MSG]]", message));
    }
    
    String url = db.getConfig("discord_webhook");
    if(null != url) {
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
  
}
