package com.calebpower.mc.dailystreaks.shop;

import java.sql.SQLException;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.model.Denizen;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
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
    
    var guiBldr = Gui.normal()
        .setStructure(
            ". . . . . . . . .",
            ". . A . B . C . .",
            ". . 0 . 1 . 2 . .",
            ". . . . . . . . .");
    var mats = plugin.getCurrentMaterials().entrySet().iterator();
    for(var slot : denizen.getQuestSlots().entrySet()) {
      guiBldr.addIngredient(
          (char)('A' + slot.getKey().ordinal()),
          new CompletionIndicatorItem(slot.getValue()));
      var mat = mats.next();
      guiBldr.addIngredient(
          (slot.getKey().ordinal() + "").charAt(0),
          slot.getValue()
              ? new SimpleItem(new ItemBuilder(mat.getKey(), mat.getValue()))
              : new IncompleteQuestItem(plugin, mat.getKey(), mat.getValue(), slot.getKey()));
    }

    Window window = Window.single()
      .setViewer(player)
      .setTitle("Daily Streaks")
      .setGui(guiBldr.build())
      .build();

    window.open();
  }
  
}
