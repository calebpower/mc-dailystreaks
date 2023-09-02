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
import com.calebpower.mc.dailystreaks.db.Database;
import com.calebpower.mc.dailystreaks.model.Denizen;

public class StreakTracker implements Runnable {

  private AtomicBoolean maintenanceMode = new AtomicBoolean(false);
  private DailyStreaks plugin = null;
  private Database db = null;
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
      while(!thread.isInterrupted()) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long delta = cal.getTimeInMillis() - System.currentTimeMillis();

        Thread.sleep(delta);

        maintenanceMode.set(true);

        List<StringBuilder> outgoingMessages = new ArrayList<>();

        try {
          Set<Denizen> denizensWithCompletedQuests = db.getDenizensWithCompletedQuests(new Timestamp(cal.getTimeInMillis()));
          Set<Denizen> denizensWithIncompleteQuests = db.getDenizensWithIncompleteQuests();

          StringBuilder currentSB = null;

          if(!denizensWithIncompleteQuests.isEmpty()) {
            outgoingMessages.add(currentSB = new StringBuilder("**Lost Streaks:**\n\n"));
            for(var denizen : denizensWithIncompleteQuests) {
              int oldStreak = denizen.getStreak();
              denizen.rstStreak();
              denizen.rstSlots();
              db.setDenizen(denizen);
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
            db.setDenizen(denizen);
          }

          for(var sb : outgoingMessages)
            plugin.broadcast(sb.toString());
          
        } catch(IOException | SQLException e) {
          e.printStackTrace();
        }
        
      }
    } catch(InterruptedException e) { }
  }

}
