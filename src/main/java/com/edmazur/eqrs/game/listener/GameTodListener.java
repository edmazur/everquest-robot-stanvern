package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class GameTodListener implements EqLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.TOD;

  private final Discord discord;
  private final GameTodDetector gameTodDetector;

  public GameTodListener(Discord discord, GameTodDetector gameTodDetector) {
    this.discord = discord;
    this.gameTodDetector = gameTodDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gameTodDetector.containsTod(eqLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "⏲ Possible ToD sighting, ET: `" + eqLogEvent.getFullLine() + "`");
    }
  }

}
