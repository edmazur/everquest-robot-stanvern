package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class TrackingListener implements EqLogListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TRACKING;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_TRACKING;

  private final Config config;
  private final Discord discord;
  private final TrackingDetector trackingDetector;

  public TrackingListener(Config config, Discord discord, TrackingDetector trackingDetector) {
    this.config = config;
    this.discord = discord;
    this.trackingDetector = trackingDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (trackingDetector.containsTracking(eqLogEvent)) {
      discord.sendMessage(
          getChannel(), "👁️ Possible TrackBot info, ET: `" + eqLogEvent.getFullLine() + "`");
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
