package com.calebpower.mc.dailystreaks;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.calebpower.mc.dailystreaks.command.BroadcastSubcommand;
import com.calebpower.mc.dailystreaks.command.ConfigSubcommand;
import com.calebpower.mc.dailystreaks.command.HelpSubcommand;
import com.calebpower.mc.dailystreaks.command.RefreshSubcommand;
import com.calebpower.mc.dailystreaks.command.Subcommand;
import com.calebpower.mc.dailystreaks.command.TokenList;
import com.calebpower.mc.dailystreaks.command.UISubcommand;
import com.calebpower.mc.dailystreaks.command.Subcommand.SubcommandException;
import com.calebpower.mc.dailystreaks.db.Database;
import com.calebpower.mc.dailystreaks.model.ValidMaterial;
import com.calebpower.mc.dailystreaks.shop.StreakTracker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
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
      config.put("msg_prefix", null == prefix ? "&d[&7DailyStreaks&d] &f" : prefix);
      config.put("discord_webhook", db.getConfig("discord_webhook"));
      config.put("bc_command", db.getConfig("bc_command"));
      config.put("reward_period", db.getConfig("reward_period"));
      config.put("reward_command", db.getConfig("reward_command"));
      config.put("little_prize", db.getConfig("little_prize"));
      
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
    commands.add(new BroadcastSubcommand(this));
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

  @Override public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
    Subcommand command = null;
    
    List<Entry<Subcommand, String[]>> subcommands = new ArrayList<>();
    for(Subcommand sc : this.commands) {
      for(var cmdarr : sc.getSubcommands())
        if(args.length >= cmdarr.length) {
          subcommands.add(
              new SimpleEntry<>(sc, cmdarr));
        }
    }
    
    for(int i = 0; i < args.length; i++) {
      for(int j = subcommands.size() - 1; j >= 0; j--) {
        String[] cmdarr = subcommands.get(j).getValue();
        if(cmdarr.length > i && !cmdarr[i].equalsIgnoreCase(args[i]))
          subcommands.remove(j);
      }
    }
    
    int argCount = -1;
    for(var sc : subcommands)
      if(sc.getValue().length > argCount) {
        argCount = sc.getValue().length;
        command = sc.getKey();
      }
    
    boolean success = false; // assume the worst
    
    if(null != command) {
      if(!command.hasPermission(sender)) { // tell viper off for trying to execute commands he doesn't need
        message(sender, "&cYou don't appear to have permission to use this command.");
      } else {
        try {
          command.onCommand( // fire the subcommand
              sender,
              new TokenList(args, argCount));
          success = true; // assume the subcommand worked because it should have thrown an exception otherwise
        } catch(SubcommandException e) { // gg no re
          if(null != e.getMessage())
            message(sender, e.getMessage());
          success = !e.doDisplayHelp();
        }
      }
    }
    
    if(!success) // the player is dumb so remind them how commands work
      message(sender, "&7If you need some help, just run &d/streak help&7.");
    
    return true;
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
                        ValidMaterial.values().length)]
                .name());
      } while(currentMaterials.keySet().contains(material));

      currentMaterials.put(
          material,
          SecureRandom.getInstanceStrong().nextInt(
              1,
              material.getMaxStackSize() + 1));

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

    if(null != hover)
      component.setHoverEvent(
          new HoverEvent(
              HoverEvent.Action.SHOW_TEXT,
              new Text(
                  ChatColor.translateAlternateColorCodes(
                      '&',
                      "&r&b" + hover))));

    sender.spigot().sendMessage(component);
  }
  
  public void broadcastDiscord(String message) throws IOException, SQLException {
    String url = db.getConfig("discord_webhook");
    if(null != url) {
      RequestBody body = RequestBody.create(
          new JSONObject().put("content", message).toString(),
          JSON);
      Request req = new Request.Builder()
          .url(url)
          .post(body)
          .build();
      
      try(Response res = http.newCall(req).execute()) {
        res.close();
      }
    }
  }
  
  public void broadcastIngame(String message) throws SQLException {
    String bcCmd = db.getConfig("bc_command");
    if(null != bcCmd) {
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
        @Override public void run() {
          Bukkit.getServer().dispatchCommand(
              Bukkit.getConsoleSender(),
              bcCmd.replace("[[MSG]]", message));
        }
      });
    }
  }

  public void broadcast(String message) throws IOException, SQLException {
    broadcastIngame(message);
    broadcastDiscord(message);
  }
  
}
