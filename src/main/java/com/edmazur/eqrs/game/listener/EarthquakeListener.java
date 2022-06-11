package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class EarthquakeListener implements EqLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.RAIDER_CHAT;

  private final Discord discord;
  private final EarthquakeDetector earthquakeDetector;

  public EarthquakeListener(Discord discord, EarthquakeDetector earthquakeDetector) {
    this.discord = discord;
    this.earthquakeDetector = earthquakeDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (earthquakeDetector.containsEarthquake(eqLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "@everyone️ Possible earthquake, ET: `" + eqLogEvent.getFullLine() + "`");
    }
  }

}
