package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.util.List;

public class EarthquakeListener implements EqLogListener {

  private static final List<DiscordChannel> PROD_CHANNELS = List.of(
      DiscordChannel.FOW_RAIDER_CHAT,
      DiscordChannel.GG_MEMBERS_CHAT);
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;
  private final EarthquakeDetector earthquakeDetector;

  public EarthquakeListener(Config config, Discord discord, EarthquakeDetector earthquakeDetector) {
    this.config = config;
    this.discord = discord;
    this.earthquakeDetector = earthquakeDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (earthquakeDetector.containsEarthquake(eqLogEvent)) {
      for (DiscordChannel discordChannel : getChannels()) {
        discord.sendMessage(
            discordChannel,
            "@everyone️ Possible earthquake, ET: `" + eqLogEvent.getFullLine() + "`");
      }
    }
  }

  private List<DiscordChannel> getChannels() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return List.of(TEST_CHANNEL);
    } else {
      return PROD_CHANNELS;
    }
  }

}
