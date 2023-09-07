package com.calebpower.mc.dailystreaks.shop;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

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
      
      /*
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
      */
      
      while(!thread.isInterrupted()) {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long delta = cal.getTimeInMillis() - System.currentTimeMillis();

        /*
        if(!runNow) Thread.sleep(delta);
        else runNow = false;
        */
        Thread.sleep(delta);
        
        try {
          Set<Denizen> goodDenizens = plugin.getDB().getDenizensWithCompletedQuests();
          Set<Denizen> badDenizens = plugin.getDB().getDenizensWithIncompleteQuests();

          if(badDenizens.isEmpty()) {
            plugin.broadcast("Fantastic! Nobody's lost their streak today! ^^");
            
          } else if(!badDenizens.isEmpty()) {
            List<StringBuilder> outgoingMessages = new ArrayList<>();
            StringBuilder currentSB = null;
            int badDenizenCount = 0;
          
            outgoingMessages.add(currentSB = new StringBuilder("**Lost Streaks:**\n"));
            for(var denizen : badDenizens) {
              int oldStreak = denizen.getStreak();
              denizen.rstStreak();
              denizen.rstSlots();
              plugin.getDB().setDenizen(denizen);

              if(0 < oldStreak) {
                badDenizenCount++;
                String bullet = String.format(
                    "- %1$s (from %2$d)\n",
                    denizen.getIGN(),
                    oldStreak);
                if(2000 < currentSB.length() + bullet.length()) {
                  currentSB.deleteCharAt(currentSB.length() - 1);
                  outgoingMessages.add(currentSB = new StringBuilder(bullet));
                } else currentSB.append(bullet);
              }
            }

            if(0 < badDenizenCount) {
              plugin.broadcastIngame(
                  String.format(
                      "%1$d people lost their daily streak%2$s! ;-;",
                      badDenizenCount,
                      1 == badDenizenCount ? "" : "s"));
              currentSB.deleteCharAt(currentSB.length() - 1);
              for(var sb : outgoingMessages)
                plugin.broadcastDiscord(sb.toString());
            }
          }

          for(var denizen : goodDenizens) {
            denizen.incStreak();
            denizen.rstSlots();
            plugin.getDB().setDenizen(denizen);
          }

          plugin.refreshMaterials();
          plugin.getDB().setConfig("last_cycle", Long.toString(System.currentTimeMillis()));
          
        } catch(IOException | NoSuchAlgorithmException | SQLException e) {
          e.printStackTrace();
        }
        
      }
    } catch(InterruptedException e) { }
  }

}
