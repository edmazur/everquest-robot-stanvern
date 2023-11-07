package com.edmazur.eqrs.game.listeners.passive;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordUser;
import java.time.Duration;
import java.time.LocalDateTime;

public class HeartbeatListener implements EqLogListener, Runnable {

  private static final Logger LOGGER = new Logger();
  // Notify if activity hasn't been seen in this amount of time.
  private static final Duration ACTIVITY_THRESHOLD = Duration.ofMinutes(5);

  // Notify if the previous was more than this amount of time ago.
  private static final Duration NOTIFICATION_THRESHOLD = Duration.ofHours(1);

  private static LocalDateTime lastSeenActivity = LocalDateTime.MAX;
  private static LocalDateTime lastSentNotification = LocalDateTime.MIN;

  public HeartbeatListener() {
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    lastSeenActivity = eqLogEvent.getTimestamp();
  }

  @Override
  public void run() {
    try {
      LocalDateTime mustHaveSeenActivityAfter = LocalDateTime.now().minus(ACTIVITY_THRESHOLD);
      LocalDateTime mustHaveSentNotificationBefore =
          LocalDateTime.now().minus(NOTIFICATION_THRESHOLD);
      if (lastSeenActivity.isBefore(mustHaveSeenActivityAfter)
          && lastSentNotification.isBefore(mustHaveSentNotificationBefore)) {
        Duration durationSinceLastActivity =
            Duration.between(lastSeenActivity, LocalDateTime.now());
        String message = String.format("No EQ log activity seen in %d minutes",
            durationSinceLastActivity.toMinutes());
        Discord.getDiscord().sendMessage(DiscordUser.EDMAZUR, message);
        lastSentNotification = LocalDateTime.now();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
