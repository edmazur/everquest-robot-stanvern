package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class TickListener implements EqLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.RAID_TICKS_AND_GRATSS;

  private final Discord discord;
  private final TickDetector tickDetector;

  public TickListener(Discord discord, TickDetector tickDetector) {
    this.discord = discord;
    this.tickDetector = tickDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (tickDetector.containsTick(eqLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "🎟️ Possible tick sighting, ET: `" + eqLogEvent.getFullLine() + "`");
    }
  }

}
