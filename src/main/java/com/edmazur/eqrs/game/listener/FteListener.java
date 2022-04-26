package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordUser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FteListener implements EqLogListener {

  private static final boolean SEND_DISCORD_MESSAGE_AS_DM = true;

  private static final Pattern FTE_PATTERN = Pattern.compile(".+ engages \\w+!");

  private final Discord discord;

  public FteListener(Discord discord) {
    this.discord = discord;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    Matcher matcher = FTE_PATTERN.matcher(eqLogEvent.getPayload());
    if (matcher.matches()) {
      String message = "FTE notice! ET: `" + eqLogEvent.getFullLine() + "`";
      if (SEND_DISCORD_MESSAGE_AS_DM) {
        discord.sendMessage(DiscordUser.EDMAZUR, message);
      } else {
        discord.sendMessage(DiscordChannel.RAIDER_CHAT, message);
      }
    }
  }

}
