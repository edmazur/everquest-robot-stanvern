package com.edmazur.eqrs.game.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameLogEvent;

public class MotdListener implements GameLogListener {

  private static final Pattern MOTD_PATTERN =
      Pattern.compile("GUILD MOTD: .+ - .+");

  private final Discord discord;

  public MotdListener(Discord discord) {
    this.discord = discord;
  }

  @Override
  public String getConfig() {
    return "";
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    Matcher matcher = MOTD_PATTERN.matcher(gameLogEvent.getText());
    if (matcher.matches()) {
      // TODO: Avoid repeating the same MotD when you manually /get or login.
      // TODO: Maybe avoid sending multiple MotDs in quick succession (e.g. from
      // fixing typos) by waiting a bit and only sending latest MotD.
      discord.sendMessage(
          DiscordChannel.RAIDER_GMOTD,
          "`" + gameLogEvent.getText() + "`");
    }
  }

}