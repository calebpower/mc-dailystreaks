package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.shop.ShopUI;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class UISubcommand extends Subcommand {
  
  public UISubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "opens the streak shop",
        CommandPermission.STANDARD_PLAYER,
        new String[][] { { "ui" }, { "gui" }, { "shop" } });
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    if(!args.isEmpty()) throw new SubcommandException(true);
    if(!(sender instanceof Player))
      throw new SubcommandException(false, "You must be in game to execute this command.");
    
    ShopUI ui = new ShopUI(getPlugin(), (Player)sender);
    
    try {
      ui.open();
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
