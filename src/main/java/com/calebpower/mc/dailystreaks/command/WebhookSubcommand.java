package com.calebpower.mc.dailystreaks.command;

import com.calebpower.mc.dailystreaks.DailyStreaks;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class WebhookSubcommand extends Subcommand {
  
  public WebhookSubcommand(DailyStreaks plugin) {
    super(
        plugin,
        "views or sets the webhook URL",
        CommandPermission.PLUGIN_ADMINISTRATOR,
        new String[] { "webhook" },
        "[url]");
  }
  
  @Override public void onCommand(CommandSender sender, TokenList args) throws SubcommandException {
    try {

      if(args.isEmpty()) {
        String webhook = getPlugin().getDB().getConfig("discord_webhook");
        
        sender.spigot().sendMessage(
            new TextComponent(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "&d[&7DailyStreak&d] &7Discord Webhook:")));

        if(null == webhook)
          webhook = "&c-- unset --";
        
        sender.spigot().sendMessage(
            new TextComponent(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "&d[&7pDailyStreakd&d] &7" + webhook)));
        
      } else if(1 == args.size()) {
        String webhook = args.get(0);

        getPlugin().getDB().setConfig("discord_webhook", webhook);

        sender.spigot().sendMessage(
            new TextComponent(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "&d[&7DailyStreak&d] &aDiscord webhook updated!")));
        
      } else throw new SubcommandException(true);
      
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
