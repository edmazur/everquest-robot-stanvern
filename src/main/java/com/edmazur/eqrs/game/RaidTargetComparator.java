package com.edmazur.eqrs.game;

import com.edmazur.eqrs.Logger;
import java.time.Instant;
import java.util.Comparator;

/**
 * This is only expected to be used to sort raid targets with the same active window status. In the
 * event though that it's used to compares raid targets with different statuses, that's the first
 * criteria checked.
 *
 * <p>For the expected case where active window statuses are the same, sorting is done by:
 * - NOW: Window end (soonest first)
 * - SOON: Window start (soonest first)
 * - LATER: Window start (soonest first)
 * - PAST: (undefined)
 * Tiebreakers are by name.
 *
 */
// TODO: Add unit tests.
// TODO: Simplify this with Guava comparator utility.
public class RaidTargetComparator implements Comparator<RaidTarget> {

  private static final Logger LOGGER = new Logger();

  private final Instant now;

  public RaidTargetComparator(Instant now) {
    this.now = now;
  }

  @Override
  public int compare(RaidTarget raidTarget1, RaidTarget raidTarget2) {
    Window raidTarget1ActiveWindow = Window.getActiveWindow(raidTarget1.getWindows(), now);
    Window raidTarget2ActiveWindow = Window.getActiveWindow(raidTarget2.getWindows(), now);
    if (raidTarget1ActiveWindow.getStatus(now)
        .compareTo(raidTarget2ActiveWindow.getStatus(now)) == 0) {
      int tiebreaker = raidTarget1.getName().compareTo(raidTarget2.getName());
      switch (raidTarget1ActiveWindow.getStatus(now)) {
        case NOW:
          if (raidTarget1ActiveWindow.getEnd().compareTo(raidTarget2ActiveWindow.getEnd()) == 0) {
            return tiebreaker;
          } else {
            return raidTarget1ActiveWindow.getEnd().compareTo(raidTarget2ActiveWindow.getEnd());
          }
        case SOON:
        case LATER:
          if (raidTarget1ActiveWindow.getStart()
              .compareTo(raidTarget2ActiveWindow.getStart()) == 0) {
            return tiebreaker;
          } else {
            return raidTarget1ActiveWindow.getStart().compareTo(raidTarget2ActiveWindow.getStart());
          }
        case PAST:
          return tiebreaker;
        default:
          return tiebreaker;
      }
    } else {
      LOGGER.log("Warning: Compared two RaidTarget objects with different statuses. This isn't "
          + "necessarily a problem, but it is unexpected, so this warning is worth looking "
          + "into since something unexpected is happening.");
      return raidTarget1ActiveWindow.getStatus(now)
          .compareTo(raidTarget2ActiveWindow.getStatus(now));
    }
  }

}
