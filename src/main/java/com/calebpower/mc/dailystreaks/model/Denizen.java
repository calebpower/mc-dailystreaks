package com.calebpower.mc.dailystreaks.model;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Denizen {

  private UUID id = null;
  private String ign = null;
  private Map<QuestSlot, Boolean> questSlots = null;
  private Timestamp lastUpdate = null;
  private int streak = 0;

  /**
   * Instantiates a new {@link Denizen} object.
   *
   * @param id the unique ID of the player in question
   * @param ign the current in-game name of the player
   * @param streak the number of days in a row a player has participated
   * @param questSlots the byte associated with the raw quest slot save state
   * @param lastUpdate the last time that the model was saved to disk
   */
  public Denizen(UUID id, String ign, int streak, byte questSlots, @Nullable Timestamp lastUpdate) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(ign);
    this.id = id;
    this.ign = ign;
    this.streak = streak;
    this.lastUpdate = lastUpdate;
    this.questSlots = QuestSlot.fromRaw(questSlots);
  }

  public UUID getID() {
    return id;
  }

  public String getIGN() {
    return ign;
  }

  public void setIGN(@Nonnull String ign) {
    Objects.requireNonNull(ign);
    this.ign = ign;
  }

  public int getStreak() {
    return streak;
  }

  public void incStreak() {
    streak++;
  }

  public void rstStreak() {
    streak = 0;
  }

  public Map<QuestSlot, Boolean> getQuestSlots() {
    return Collections.unmodifiableMap(questSlots);
  }

  public void setUsed(@Nonnull QuestSlot slot) {
    Objects.requireNonNull(slot);
    questSlots.replace(slot, false, true);
  }

  public void rstSlots() {
    for(var slot : questSlots.keySet())
      questSlots.replace(slot, true, false);
  }

  public Timestamp getLastSaveTime() {
    return lastUpdate;
  }

  public void setLastSaveTime(Timestamp lastUpdate) {
    this.lastUpdate = lastUpdate;
  }
  
}
