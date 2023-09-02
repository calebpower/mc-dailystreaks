package com.calebpower.mc.dailystreaks.command;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

public class ConfigSubcommand extends Subcommand {
  
  private final Map<String, String> keys = new TreeMap<>();
  
  public ConfigSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "views or sets the webhook URL",
        CommandPermission.PLUGIN_ADMINISTRATOR,
        new String[] { "config" },
        "<key> [value]");
    keys.put("broadcast", "bc_command");
    keys.put("checkpoint", "reward_period");
    keys.put("prefix", "msg_prefix");
    keys.put("reward", "reward_command");
    keys.put("prize", "little_prize");
    keys.put("webhook", "discord_webhook");
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    try {
      
      if(args.isEmpty()) {
        
        var itr = keys.entrySet().iterator();
        while(itr.hasNext()) {
          var key = itr.next();
          String value = getPlugin().getConfig(key.getValue());
          getPlugin().message(sender, "Key = " + key.getKey());
          getPlugin().message(sender, "Val = " + (null == value ? "&e(unset)" : value));
          if(itr.hasNext())
            getPlugin().message(sender, "&8--------------------");
        }
        
      } else if(1 == args.size()) {
        
        String key = keys.get(args.get(0).toLowerCase());
        if(null == key) throw new SubcommandException(false, "&cUnknown key.");
        
        String value = getPlugin().getConfig(key);
        
        getPlugin().message(sender, "Key = " + args.get(0).toLowerCase());
        getPlugin().message(sender, "Val = " + (null == value ? "&e(unset)" : value));
        
      } else if(2 == args.size()) {
        
        String key = keys.get(args.get(0).toLowerCase());
        if(null == key) throw new SubcommandException(false, "&cUnknown key.");
        
        String value = args.get(1);
        getPlugin().getDB().setConfig(key, value);
        getPlugin().setConfig(key, value);
        getPlugin().message(sender, "&aConfig updated!");
        
      } else throw new SubcommandException(true);
      
    } catch(SQLException e) {
      e.printStackTrace();
      getPlugin().message(
          sender,
          "&cAn exception was thrown: %1$s",
          null,
          null == e.getMessage()
              ? "check the console for more info"
              : e.getMessage());
    }
  }
  
}
