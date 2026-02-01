package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordUser;
import java.time.Duration;
import java.time.LocalDateTime;

public class HeartbeatListener implements EqLogListener, Runnable {

  // Notify if activity hasn't been seen in this amount of time.
  private static final Duration ACTIVITY_THRESHOLD = Duration.ofMinutes(5);

  // Notify if the previous was more than this amount of time ago.
  private static final Duration NOTIFICATION_THRESHOLD = Duration.ofHours(1);

  private final Discord discord;

  private LocalDateTime lastSeenActivity = LocalDateTime.MAX;
  private LocalDateTime lastSentNotification = LocalDateTime.MIN;

  public HeartbeatListener(Discord discord) {
    this.discord = discord;
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

        String userMessage = String.format(
            "No EQ log activity seen in %d minutes.",
            durationSinceLastActivity.toMinutes());
        String channelMessage = userMessage + " Stanvern has been notified that his game client "
            + "likely disconnected. Missed loot and ToDs will need to be manually entered.";

        discord.sendMessage(DiscordUser.EDMAZUR, userMessage);
        discord.sendMessage(DiscordChannel.GG_GROUP_TEXT, channelMessage);
        discord.sendMessage(DiscordChannel.GG_TOD, channelMessage);
        discord.sendMessage(DiscordChannel.GG_TICKS_AND_GRATS, channelMessage)
            .join().addReaction("👍");

        lastSentNotification = LocalDateTime.now();
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
