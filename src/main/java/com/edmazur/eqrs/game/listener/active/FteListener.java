package com.edmazur.eqrs.game.listener.active;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordUser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FteListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();
  private static final boolean SEND_DISCORD_MESSAGE_AS_DM = true;

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_RAID_DISCUSSION;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private static final Pattern FTE_PATTERN = Pattern.compile(".+ engages \\w+!");


  public FteListener() {
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    Matcher matcher = FTE_PATTERN.matcher(eqLogEvent.getPayload());
    if (matcher.matches()) {
      String message = "FTE notice! ET: `" + eqLogEvent.getFullLine() + "`";
      if (SEND_DISCORD_MESSAGE_AS_DM) {
        Discord.getDiscord().sendMessage(DiscordUser.EDMAZUR, message);
      } else {
        Discord.getDiscord().sendMessage(getChannel(), message);
      }
    }
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().isDebug()) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
