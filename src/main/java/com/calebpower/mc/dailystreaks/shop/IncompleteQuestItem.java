package com.calebpower.mc.dailystreaks.shop;

import java.io.IOException;
import java.sql.SQLException;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.model.Denizen;
import com.calebpower.mc.dailystreaks.model.QuestSlot;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class IncompleteQuestItem extends AbstractItem {

  private DailyStreaks plugin = null;
  private Material material = null;
  private QuestSlot slot = null;
  private int quantity = 0;

  public IncompleteQuestItem(DailyStreaks plugin, Material material, int quantity, QuestSlot slot) {
    super();
    this.plugin = plugin;
    this.material = material;
    this.quantity = quantity;
    this.slot = slot;
  }

  @Override public ItemProvider getItemProvider() {
    return new ItemBuilder(material, quantity);
  }
  
  @Override public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
    var inv = player.getInventory();
    var contents = inv.getContents();

    int count = 0;
    
    for(int i = 0; i < contents.length && count < quantity; i++) {
      if(isValidItem(contents[i]))
        count += contents[i].getAmount();
    }

    if(count < quantity) { // player did not have enough of whatever
      player.spigot().sendMessage(
          new TextComponent(
              ChatColor.translateAlternateColorCodes(
                  '&',
                  "&d[&7DailyStreak&d] &cYou don't have enough of that item.")));
                      
    } else {
      count = quantity;
      
      for(int i = 0; 0 < count && i < contents.length; i++) {
        if(isValidItem(contents[i])) {
          if(contents[i].getAmount() <= count) {
            count -= contents[i].getAmount();
            inv.setItem(i, null);
          } else {
            contents[i].setAmount(contents[i].getAmount() - count);
            count = 0;
          }
        }
      }

      try {
        Denizen denizen = plugin.getDB().getDenizen(player.getUniqueId());
        if(null == denizen) {
          denizen = new Denizen(
              player.getUniqueId(),
              player.getName(),
              0,
              (byte)0x0,
              null);
        } else denizen.setIGN(player.getName());

        denizen.setUsed(slot);
        plugin.getDB().setDenizen(denizen);

        long remaining = denizen.getQuestSlots().entrySet().stream().filter(s -> !s.getValue()).count();
        if(0 == remaining) {
          player.spigot().sendMessage(
              new TextComponent(
                  ChatColor.translateAlternateColorCodes(
                      '&',
                      String.format(
                          "&d[&7DailyStreak&d] &aFantastic! Your streak count has increased to %1$d!",
                          denizen.getStreak() + 1))));
          plugin.publishMessage(
              String.format(
                  "%1$s's streak has increased to %2$d!",
                  player.getName(),
                  denizen.getStreak() + 1));
        } else {
          player.spigot().sendMessage(
              new TextComponent(
                  ChatColor.translateAlternateColorCodes(
                      '&',
                      String.format(
                          "&d[&7DailyStreak&d] &aGreat! You only have %1$d more quest%2$s left to go today!",
                          remaining,
                          1 == remaining ? "" : "s"))));
        }
        
      } catch(IOException | SQLException e) {
        e.printStackTrace();
        player.spigot().sendMessage(
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

  private boolean isValidItem(ItemStack stack) {
    return stack.getType() == material
      && !stack.getItemMeta().hasEnchants()
      && !stack.getItemMeta().hasDisplayName();
  }
  
}
