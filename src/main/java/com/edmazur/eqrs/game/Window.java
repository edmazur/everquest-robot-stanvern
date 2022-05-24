package com.edmazur.eqrs.game;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class Window {

  // Note that the order of these is important. It's used when sorting to find the "active" window.
  public enum Status {
    NOW,
    SOON,
    LATER,
    PAST,
    ;

    // Keep these 2 constants in sync.
    // Visible for testing.
    static final Duration SOON_THRESHOLD = Duration.ofHours(48);
    public static final String SOON_DESCRIPTION = "48 hours";
  }

  private final Instant start;
  private final Instant end;

  public Window(Instant start, Instant end) {
    this.start = start;
    this.end = end;
  }

  public Instant getStart() {
    return start;
  }

  public Instant getEnd() {
    return end;
  }

  public Status getStatus(Instant now) {
    if (end.isBefore(now)) {
      return Status.PAST;
    } else if (start.isBefore(now) && end.isAfter(now)) {
      return Status.NOW;
    } else if (start.isBefore(now.plus(Status.SOON_THRESHOLD))) {
      return Status.SOON;
    } else {
      return Status.LATER;
    }
  }

  /**
   * Returns the "active" window.
   *
   * <p>The active window is the first window with the "lowest" Status (by enum order).
   */
  public static Window getActiveWindow(List<Window> windows, Instant now) {
    Status activeWindowStatus = getActiveWindowStatus(windows, now);
    for (Window window : windows) {
      if (window.getStatus(now) == activeWindowStatus) {
        return window;
      }
    }
    throw new IllegalStateException("Could not find active window: " + windows);
  }

  /**
   * Returns the status of the "active" window.
   *
   * <p>The active window is the first window with the "lowest" Status (by enum order).
   */
  // Visible for testing.
  static Status getActiveWindowStatus(List<Window> windows, Instant now) {
    SortedSet<Window.Status> windowStatuses = new TreeSet<>();
    for (Window window : windows) {
      windowStatuses.add(window.getStatus(now));
    }
    return windowStatuses.first();
  }

  @Override
  public String toString() {
    return String.format("[%s-%s]", start, end);
  }

}
