package com.calebpower.mc.dailystreaks.command;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

public class RefreshSubcommand extends Subcommand {
  
  public RefreshSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "resets the current set of shop items",
        CommandPermission.PLUGIN_ADMINISTRATOR,
        new String[][] {{ "refresh" }, { "reset" }});
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    if(!args.isEmpty()) throw new SubcommandException(true);
    
    try {
      getPlugin().refreshMaterials();
      getPlugin().message(sender, "&aSuccessfully refreshed materials.");
    } catch(NoSuchAlgorithmException | SQLException e) {
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
