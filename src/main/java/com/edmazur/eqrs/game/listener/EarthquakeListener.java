package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.RateLimiter;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Duration;

public class EarthquakeListener implements EqLogListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_BATPHONE;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_BATPHONE;

  // This rate limit is just to safeguard against repeated false positive detection (code bug, GM
  // trolling, etc.), which would result in repeated batphones. 1 hour is somewhat arbitrary - long
  // enough that it shouldn't result in spam, but short enough where some edge case scenario where
  // it quakes twice in a day ishandled correctly.
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
        discord.sendMessage(
            getChannel(), "@everyone QUAKE" + "\n" + "(ET: `" + eqLogEvent.getFullLine() + "`");
      }
    }
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
