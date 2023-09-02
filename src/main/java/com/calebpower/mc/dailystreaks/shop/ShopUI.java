package com.calebpower.mc.dailystreaks.shop;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.model.Denizen;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.md_5.bungee.api.ChatColor;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class ShopUI implements Listener {
  
  private DailyStreaks plugin = null;
  private Player player = null;
  
  public ShopUI(DailyStreaks plugin, Player player) {
    this.plugin = plugin;
    this.player = player;
  }
  
  public void open() throws SQLException {
    Denizen denizen = plugin.getDB().getDenizen(player.getUniqueId());

    if(null == denizen) {
      denizen = new Denizen(
          player.getUniqueId(),
          player.getName(),
          0,
          (byte)0x0,
          null);
      plugin.getDB().setDenizen(denizen);
    } else if(!denizen.getIGN().equals(player.getName())) {
      denizen.setIGN(player.getName());
      plugin.getDB().setDenizen(denizen);
    }
    
    var guiBldr = Gui.normal()
        .setStructure(
            ". . . . . . . . .",
            ". . A . B . C . .",
            ". . 0 . 1 . 2 . .",
            ". . . . . . . . .");
    var mats = plugin.getCurrentMaterials().entrySet().iterator();

    Set<IncompleteQuestItem> incompleteQuestItems = new HashSet<>();
    
    for(var slot : denizen.getQuestSlots().entrySet()) {
      guiBldr.addIngredient(
          (char)('A' + slot.getKey().ordinal()),
          new CompletionIndicatorItem(slot.getValue()));
      var mat = mats.next();
      AbstractItem item = null;
      guiBldr.addIngredient(
          (slot.getKey().ordinal() + "").charAt(0),
          item = slot.getValue()
              ? new SimpleItem(new ItemBuilder(mat.getKey(), mat.getValue()))
              : new IncompleteQuestItem(plugin, mat.getKey(), mat.getValue(), slot.getKey()));
      if(!slot.getValue()) incompleteQuestItems.add((IncompleteQuestItem)item);
    }

    Window window = Window.single()
      .setViewer(player)
      .setTitle(
          ChatColor.translateAlternateColorCodes('&', "&dClick an Item to Trade In!"))
      .setGui(guiBldr.build())
      .build();

    for(var item : incompleteQuestItems)
      item.setWindow(window);

    window.open();
  }
  
}
