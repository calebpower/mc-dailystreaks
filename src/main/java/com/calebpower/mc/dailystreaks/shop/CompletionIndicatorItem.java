package com.calebpower.mc.dailystreaks.shop;

import org.bukkit.Material;

import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AutoCycleItem;

public class CompletionIndicatorItem extends AutoCycleItem {

  public CompletionIndicatorItem(boolean isComplete) {
    super(
        1,
        isComplete
        ? new ItemProvider[] {
          new ItemBuilder(Material.GREEN_CANDLE).setDisplayName("Complete! :)")
        }
        : new ItemProvider[] {
          new ItemBuilder(Material.RED_CANDLE).setDisplayName("Incomplete :("),
          new ItemBuilder(Material.ORANGE_CANDLE).setDisplayName("Incomplete :(")
        });
  }
  
}
