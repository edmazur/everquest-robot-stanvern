package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.RateLimiter;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import org.javacord.api.entity.message.Message;

public class EarthquakeListener implements EqLogListener {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("M/d HH:mm:ss");

  private static final DiscordChannel PROD_BATPHONE_CHANNEL = DiscordChannel.GG_BATPHONE;
  private static final DiscordChannel TEST_BATPHONE_CHANNEL = DiscordChannel.TEST_BATPHONE;

  private static final DiscordChannel PROD_TOD_CHANNEL = DiscordChannel.GG_TOD;
  private static final DiscordChannel TEST_TOD_CHANNEL = DiscordChannel.TEST_TOD;

  // This rate limit is just to safeguard against repeated false positive detection (code bug, GM
  // trolling, etc.), which would result in repeated batphones. 1 hour is somewhat arbitrary - long
  // enough that it shouldn't result in spam, but short enough where some edge case scenario where
  // it quakes twice in a day is handled correctly.
  private static final Duration RATE_LIMIT = Duration.ofHours(1);

  private final Config config;
  private final Discord discord;
  private final EarthquakeDetector earthquakeDetector;

  private final RateLimiter rateLimiter = new RateLimiter(RATE_LIMIT);

  public EarthquakeListener(Config config, Discord discord, EarthquakeDetector earthquakeDetector) {
    this.config = config;
    this.discord = discord;
    this.earthquakeDetector = earthquakeDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (earthquakeDetector.containsEarthquake(eqLogEvent)) {
      if (rateLimiter.getPermission()) {
        // Send batphone.
        discord.sendMessage(
            getBatphoneChannel(),
            "@everyone QUAKE" + "\n" + "(ET: `" + eqLogEvent.getFullLine() + "`)");

        // Log the earthquake in the ToD channel.
        Message earthquakeLogMessage = discord
            .sendMessage(
                getTodChannel(),
                String.format("ET: `%s`", eqLogEvent.getFullLine()))
            .join();

        // Enter the !quake command.
        earthquakeLogMessage.reply(
            String.format("!quake %s", DATE_TIME_FORMATTER.format(eqLogEvent.getTimestamp())));

        // Enter Ring 8 "ToD".
        earthquakeLogMessage.reply(
            String.format(
                "!tod ring 8, %s",
                DATE_TIME_FORMATTER.format(eqLogEvent.getTimestamp().minusMinutes(30))));

        // Enter Vaniki ToD.
        earthquakeLogMessage.reply(
            String.format(
                "!tod vaniki, %s", DATE_TIME_FORMATTER.format(eqLogEvent.getTimestamp())));
      }
    }
  }

  private DiscordChannel getBatphoneChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_BATPHONE_CHANNEL;
    } else {
      return PROD_BATPHONE_CHANNEL;
    }
  }

  private DiscordChannel getTodChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_TOD_CHANNEL;
    } else {
      return PROD_TOD_CHANNEL;
    }
  }

}
