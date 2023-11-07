package com.edmazur.eqrs.game.listeners.passive;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.listeners.GratsDetector;
import com.edmazur.eqrs.game.listeners.GratsParseResult;
import com.edmazur.eqrs.game.listeners.GratsParser;
import org.javacord.api.entity.message.MessageBuilder;

public class GratsListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();
  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TICKS_AND_GRATS;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final GratsDetector gratsDetector;
  private final GratsParser gratsParser;

  public GratsListener() {
    this.gratsDetector = new GratsDetector();
    this.gratsParser = new GratsParser();
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gratsDetector.containsGrats(eqLogEvent)) {
      GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);
      Discord.getDiscord().sendMessage(getChannel(),
          gratsParseResult.prepareForCreate(new MessageBuilder()));
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
