package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameLogEvent;

public class TickListener implements GameLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.RAID_TICKS;

  private final Discord discord;
  private final TickDetector tickDetector;

  public TickListener(Discord discord, TickDetector tickDetector) {
    this.discord = discord;
    this.tickDetector = tickDetector;
  }

  @Override
  public String getConfig() {
    return "";
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    if (tickDetector.containsTick(gameLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "Possible tick sighting, ET: `" + gameLogEvent.getFullLogLine() + "`");
    }
  }

}
