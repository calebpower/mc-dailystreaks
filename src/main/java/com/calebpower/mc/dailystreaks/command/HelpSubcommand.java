/*
 * Copyright (c) 2023 Caleb L. Power. All rights reserved.
 */
package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * Subcommand designed to help out a little.
 * 
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class HelpSubcommand extends Subcommand {
  
  /**
   * Instantiates this subcommand.
   * 
   * @param plugin the {@link PaperLotto} instance
   */
  public HelpSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "display subcommand usage",
        CommandPermission.STANDARD_PLAYER,
        new String[] { "help" });
  }

  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    sender.spigot().sendMessage(
        new TextComponent(
            ChatColor.translateAlternateColorCodes(
                '&',
                "&d[&fDailyStreak&d] &7Usage:")));
    
    for(var sc : getPlugin().getSubcommands()) {
      // don't display usage to a player that doesn't have the authority to execute it
      if(!sc.hasPermission(sender)) continue;
      
      // start building the string or whatever
      StringBuilder sb = new StringBuilder("/streak");
      for(var c : sc.getSubcommands()[0])
        sb.append(" ").append(c);
      
      // send it off to the player and add some interactive components
      TextComponent usage = new TextComponent(
          ChatColor.translateAlternateColorCodes(
              '&',
              String.format(
                  "&d[&fDailyStreak&d] &7%1$s%2$s",
                  sb.toString(),
                  sc.getArgUsage())));
      usage.setHoverEvent(
          new HoverEvent(
              HoverEvent.Action.SHOW_TEXT,
              new Text(
                  ChatColor.translateAlternateColorCodes(
                      '&',
                      "&r&b" + sc.getDescription()))));
      sender.spigot().sendMessage(usage);
    }
  }
  
}
