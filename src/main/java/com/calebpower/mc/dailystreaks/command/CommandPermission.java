/*
 * Copyright (c) 2023 Caleb L. Power. All rights reserved.
 */
package com.calebpower.mc.dailystreaks.command;

import org.bukkit.command.CommandSender;

/**
 * Denotes the level of privilege one must have in order to execute a particular
 * subcommand.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public enum CommandPermission {

  /**
   * The player is not an admin but is authorized to execute player commands.
   */
  STANDARD_PLAYER("dailystreaks.player"),

  /**
   * The player is authorized to administrate this iplugin.
   */
  PLUGIN_ADMINISTRATOR("dailystreaks.admin");

  private String permission = null;

  private CommandPermission(String permission) {
    this.permission = permission;
  }

  /**
   * Determines whether or not a player has at least the specified authority.
   *
   * @param sender the {@link CommandSender} executing the command
   * @return {@code true} iff the sender has the proper authority
   */
  public boolean hasPermission(CommandSender sender) {
    // if op or full/plugin star perms, then they're good
    if(sender.isOp()
       || sender.hasPermission("*")
       || sender.hasPermission("dailystreaks.*"))
      return true;

    // otherwise, if they have a perm with an ordinal at or lower than the
    // comparison, they're also good
    for(int i = ordinal(); i < values().length; i++)
      if(sender.hasPermission(values()[i].permission))
        return true;

    // if none of the above, YEET!
    return false;
  }
  
}
