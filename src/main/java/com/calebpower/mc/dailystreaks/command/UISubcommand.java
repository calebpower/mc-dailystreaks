package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.shop.ShopUI;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
