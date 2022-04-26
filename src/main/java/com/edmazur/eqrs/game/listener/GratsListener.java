package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class GratsListener implements EqLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.RAID_TICKS_AND_GRATSS;

  private final Discord discord;
  private final GratsDetector gratsDetector;

  public GratsListener(Discord discord, GratsDetector gratsDetector) {
    this.discord = discord;
    this.gratsDetector = gratsDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gratsDetector.containsGrats(eqLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "💰 Possible gratss sighting, ET: `" + eqLogEvent.getFullLine() + "`");
    }
  }

}
