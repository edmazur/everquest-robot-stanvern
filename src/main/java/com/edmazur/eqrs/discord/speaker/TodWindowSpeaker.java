package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.discord.DiscordTableFormatter;
import com.edmazur.eqrs.game.RaidTargetTableMaker;
import com.edmazur.eqrs.table.Table;

public class TodWindowSpeaker implements Runnable {

  private static final DiscordChannel CHANNEL = DiscordChannel.TIMERS;

  private final Config config;
  private final Discord discord;
  private final RaidTargetTableMaker raidTargetTableMaker;
  private final DiscordTableFormatter discordTableFormatter;

  public TodWindowSpeaker(
      Config config,
      Discord discord,
      RaidTargetTableMaker raidTargetTableMaker,
      DiscordTableFormatter discordTableFormatter) {
    this.config = config;
    this.discord = discord;
    this.raidTargetTableMaker = raidTargetTableMaker;
    this.discordTableFormatter = discordTableFormatter;
  }

  @Override
  public void run() {
    try {
      Table table = raidTargetTableMaker.make();
      // Delete channel contents only when *not* in debug mode. The current debug setup for Discord
      // interactions doesn't play with this well.
      // TODO: Remove this restriction once a better test setup is in place.
      if (!config.getBoolean(Config.Property.DEBUG)) {
        discord.deleteMessagesMatchingPredicate(CHANNEL, DiscordPredicate.isFromYourself());
      }
      for (String text : discordTableFormatter.format(table)) {
        discord.sendMessage(CHANNEL, text);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
