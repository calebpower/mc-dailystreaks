package com.calebpower.mc.dailystreaks.model;

import java.util.EnumMap;
import java.util.Map;

public enum QuestSlot {

  SLOT_1,
  SLOT_2,
  SLOT_3;

  public static Map<QuestSlot, Boolean> fromRaw(byte raw) {
    Map<QuestSlot, Boolean> values = new EnumMap<>(QuestSlot.class);
    for(byte i = 0x0; i < values().length; i++)
      values.put(values()[i], 0x1 == ((raw >> i) & 0x1));
    return values;
  }

  public static byte toRaw(Map<QuestSlot, Boolean> values) {
    byte raw = 0x0;
    for(var entry : values.entrySet())
      if(entry.getValue())
        raw += Math.pow(2, entry.getKey().ordinal());
    return raw;
  }
  
}
