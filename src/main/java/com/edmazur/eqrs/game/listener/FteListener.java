package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordUser;
import com.edmazur.eqrs.game.GameLogEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FteListener implements GameLogListener {

  private static final boolean SEND_DISCORD_MESSAGE_AS_DM = true;

  private static final Pattern FTE_PATTERN = Pattern.compile(".+ engages \\w+!");

  private final Discord discord;

  public FteListener(Discord discord) {
    this.discord = discord;
  }

  @Override
  public String getConfig() {
    return String.format("SEND_DISCORD_MESSAGE_AS_DM=%s", SEND_DISCORD_MESSAGE_AS_DM);
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    Matcher matcher = FTE_PATTERN.matcher(gameLogEvent.getText());
    if (matcher.matches()) {
      String message = "FTE notice! ET: `" + gameLogEvent.getFullLogLine() + "`";
      if (SEND_DISCORD_MESSAGE_AS_DM) {
        discord.sendMessage(DiscordUser.EDMAZUR, message);
      } else {
        discord.sendMessage(DiscordChannel.RAIDER_CHAT, message);
      }
    }
  }

}
