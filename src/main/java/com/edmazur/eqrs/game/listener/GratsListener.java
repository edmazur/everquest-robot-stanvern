package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.ItemScreenshotter;

public class GratsListener implements EqLogListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TICKS_AND_GRATS;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;
  private final GratsDetector gratsDetector;
  private final GratsParser gratsParser;
  private final ItemScreenshotter itemScreenshotter;

  public GratsListener(
      Config config,
      Discord discord,
      GratsDetector gratsDetector,
      GratsParser gratsParser,
      ItemScreenshotter itemScreenshotter) {
    this.config = config;
    this.discord = discord;
    this.gratsDetector = gratsDetector;
    this.gratsParser = gratsParser;
    this.itemScreenshotter = itemScreenshotter;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gratsDetector.containsGrats(eqLogEvent)) {
      GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);
      discord.sendMessage(getChannel(), gratsParseResult.getMessageBuilder(itemScreenshotter));
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
