package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.discord.DiscordTableFormatter;
import com.edmazur.eqrs.game.RaidTargetTableMaker;
import com.edmazur.eqrs.table.Table;

public class TodWindowSpeaker implements Runnable {

  private static final DiscordChannel CHANNEL = DiscordChannel.TIMERS;

  private final Discord discord;
  private final RaidTargetTableMaker raidTargetTableMaker;
  private final DiscordTableFormatter discordTableFormatter;

  public TodWindowSpeaker(
      Discord discord,
      RaidTargetTableMaker raidTargetTableMaker,
      DiscordTableFormatter discordTableFormatter) {
    this.discord = discord;
    this.raidTargetTableMaker = raidTargetTableMaker;
    this.discordTableFormatter = discordTableFormatter;
  }

  @Override
  public void run() {
    try {
      Table table = raidTargetTableMaker.make();
      discord.deleteMessagesMatchingPredicate(CHANNEL, DiscordPredicate.isFromYourself());
      for (String text : discordTableFormatter.format(table)) {
        discord.sendMessage(CHANNEL, text);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

}
