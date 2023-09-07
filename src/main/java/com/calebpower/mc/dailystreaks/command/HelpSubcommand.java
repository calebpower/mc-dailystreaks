/*
 * Copyright (c) 2023 Caleb L. Power. All rights reserved.
 */
package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

/**
 * Subcommand designed to help out a little.
 * 
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class HelpSubcommand extends Subcommand {
  
  /**
   * Instantiates this subcommand.
   * 
   * @param plugin the {@link DailyStreaks} instance
   */
  public HelpSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "display subcommand usage",
        CommandPermission.STANDARD_PLAYER,
        new String[] { "help" });
  }

  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    getPlugin().message(sender, "Usage:");
    
    for(var sc : getPlugin().getSubcommands()) {
      // don't display usage to a player that doesn't have the authority to execute it
      if(!sc.hasPermission(sender)) continue;
      
      // start building the string or whatever
      StringBuilder sb = new StringBuilder("/streak");
      for(var c : sc.getSubcommands()[0])
        sb.append(" ").append(c);
      
      // send it off to the player and add some interactive components
      getPlugin().message(
          sender,
          "%1$s %2$s",
          sc.getDescription(),
          sb.toString(),
          sc.getArgUsage());
    }
  }
  
}
