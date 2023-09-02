/*
 * Copyright (c) 2023 Caleb L. Power. All rights reserved.
 */
package com.calebpower.mc.dailystreaks.command;

import java.io.IOException;
import java.sql.SQLException;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

public class BroadcastSubcommand extends Subcommand {
  
  public BroadcastSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "broadcast a message",
        CommandPermission.PLUGIN_ADMINISTRATOR,
        new String[] { "bc" },
        "<message>");
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    if(args.isEmpty()) throw new SubcommandException(true);
    
    StringBuilder sb = new StringBuilder();
    var itr = args.iterator();
    while(itr.hasNext()) {
      sb.append(itr.next());
      if(itr.hasNext()) sb.append(' ');
    }
    
    try {
      getPlugin().broadcast(sb.toString());
    } catch(IOException | SQLException e) {
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
