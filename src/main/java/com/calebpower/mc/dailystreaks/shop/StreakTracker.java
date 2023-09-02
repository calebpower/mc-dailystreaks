package com.calebpower.mc.dailystreaks.shop;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.calebpower.mc.dailystreaks.DailyStreaks;
import com.calebpower.mc.dailystreaks.model.Denizen;

public class StreakTracker implements Runnable {

  private DailyStreaks plugin = null;
  private Thread thread = null;

  public StreakTracker(DailyStreaks plugin) {
    this.plugin = plugin;
  }

  public void start() throws NoSuchAlgorithmException, SQLException {
    if(null != thread) return;
    thread = new Thread(this);
    thread.setDaemon(true);
    thread.start();
  }

  public void stop() {
    if(null == thread) return;
    thread.interrupt();
    thread = null;
  }

  @Override public void run() {
    try {
      boolean runNow = false;

      try {
        long lastCycle = Long.parseLong(
            plugin.getDB().getConfig("last_cycle"));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        runNow = lastCycle < cal.getTimeInMillis();
        
      } catch(SQLException e) {
        e.printStackTrace();
      } catch(NumberFormatException e) { }
      
      while(!thread.isInterrupted()) {
        
        Calendar cal = Calendar.getInstance();
        /*
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        */
        cal.add(Calendar.MINUTE, 2); // XXX this is for testing only
        long delta = cal.getTimeInMillis() - System.currentTimeMillis();

        if(!runNow) Thread.sleep(delta);
        
        List<StringBuilder> outgoingMessages = new ArrayList<>();

        try {
          Set<Denizen> denizensWithCompletedQuests = plugin.getDB().getDenizensWithCompletedQuests();
          Set<Denizen> denizensWithIncompleteQuests = plugin.getDB().getDenizensWithIncompleteQuests();

          StringBuilder currentSB = null;

          if(denizensWithIncompleteQuests.isEmpty()) {
            outgoingMessages.add(new StringBuilder("Fantastic! Nobody's lost their streak today! ^^"));
          } else if(!denizensWithIncompleteQuests.isEmpty()) {
            outgoingMessages.add(currentSB = new StringBuilder("**Lost Streaks:**\n"));
            for(var denizen : denizensWithIncompleteQuests) {
              int oldStreak = denizen.getStreak();
              denizen.rstStreak();
              denizen.rstSlots();
              plugin.getDB().setDenizen(denizen);
              String bullet = String.format(
                  "- %1$s (from %2$d)\n",
                  denizen.getIGN(),
                  oldStreak);
              if(2000 < currentSB.length() + bullet.length()) {
                currentSB.deleteCharAt(currentSB.length() - 1);
                outgoingMessages.add(currentSB = new StringBuilder(bullet));
              } else currentSB.append(bullet);
            }
            
            currentSB.deleteCharAt(currentSB.length() - 1);
          }

          for(var denizen : denizensWithCompletedQuests) {
            denizen.incStreak();
            denizen.rstSlots();
            plugin.getDB().setDenizen(denizen);
          }

          for(var sb : outgoingMessages)
            plugin.broadcast(sb.toString());

          plugin.refreshMaterials();
          plugin.getDB().setConfig("last_cycle", Long.toString(System.currentTimeMillis()));
          
        } catch(IOException | NoSuchAlgorithmException | SQLException e) {
          e.printStackTrace();
        }
        
      }
    } catch(InterruptedException e) { }
  }

}
