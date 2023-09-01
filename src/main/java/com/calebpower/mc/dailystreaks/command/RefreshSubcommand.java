package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class RefreshSubcommand extends Subcommand {
  
  public RefreshSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "resets the current set of shop items",
        CommandPermission.PLUGIN_ADMINISTRATOR,
        new String[] { "reset" });
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    if(!args.isEmpty()) throw new SubcommandException(true);
    
    try {
      getPlugin().refreshMaterials();
      sender.spigot().sendMessage(
          new TextComponent(
              ChatColor.translateAlternateColorCodes(
                  '&',
                  "&d[&7DailyStreak&d] &aSuccessfully refreshed materials.")));
    } catch(Exception e) {
      e.printStackTrace();
      sender.spigot().sendMessage(
          new TextComponent(
              ChatColor.translateAlternateColorCodes(
                  '&',
                  String.format(
                      "&d[&7DailyStreak&d] &cAn exception was thrown: %1$s",
                      null == e.getMessage()
                          ? "check the console for more info"
                          : e.getMessage()))));
    }
  }
  
}
