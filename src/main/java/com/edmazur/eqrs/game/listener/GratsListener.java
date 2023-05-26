package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class GratsListener implements EqLogListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TICKS_AND_GRATS;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;
  private final GratsDetector gratsDetector;
  private final GratsParser gratsParser;

  public GratsListener(
      Config config,
      Discord discord,
      GratsDetector gratsDetector,
      GratsParser gratsParser) {
    this.config = config;
    this.discord = discord;
    this.gratsDetector = gratsDetector;
    this.gratsParser = gratsParser;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gratsDetector.containsGrats(eqLogEvent)) {
      GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);
      discord.sendMessage(getChannel(), gratsParseResult.getMessageBuilder());
    }
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
