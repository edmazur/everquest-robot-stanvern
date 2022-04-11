package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameLogEvent;

public class GameTodListener implements GameLogListener {

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.TOD;

  private final Discord discord;
  private final GameTodDetector gameTodDetector;

  public GameTodListener(Discord discord, GameTodDetector gameTodDetector) {
    this.discord = discord;
    this.gameTodDetector = gameTodDetector;
  }

  @Override
  public String getConfig() {
    return "";
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    if (gameTodDetector.containsTod(gameLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "Possible ToD sighting, ET: `" + gameLogEvent.getFullLogLine() + "`");
    }
  }

}
