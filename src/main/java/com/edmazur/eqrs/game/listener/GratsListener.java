package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameLogEvent;

public class GratsListener implements GameLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.RAID_TICKS_AND_GRATSS;

  private final Discord discord;
  private final GratsDetector gratsDetector;

  public GratsListener(Discord discord, GratsDetector gratsDetector) {
    this.discord = discord;
    this.gratsDetector = gratsDetector;
  }

  @Override
  public String getConfig() {
    return "";
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    if (gratsDetector.containsGrats(gameLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "💰 Possible gratss sighting, ET: `" + gameLogEvent.getFullLogLine() + "`");
    }
  }

}
