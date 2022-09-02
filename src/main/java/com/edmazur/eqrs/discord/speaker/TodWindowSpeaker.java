package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.discord.DiscordTableFormatter;
import com.edmazur.eqrs.game.RaidTargetTableMaker;
import com.edmazur.eqrs.table.Table;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TodWindowSpeaker implements Runnable {

  private static final List<DiscordChannel> PROD_CHANNELS = Arrays.asList(
      DiscordChannel.FOW_TIMERS,
      DiscordChannel.GG_TIMERS);
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_TIMERS;

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
      for (DiscordChannel discordChannel : getChannels()) {
        discord.deleteMessagesMatchingPredicate(discordChannel, DiscordPredicate.isFromYourself());
        for (String messages : discordTableFormatter.getMessages(table, Map.of(0, 1))) {
          // Wait for the Future to complete before sending the next message. In testing, not having
          // this in place led to out-of-order messages when they got sent in rapid succession.
          discord.sendMessage(discordChannel, messages).join();
        }
        discord.sendMessage(discordChannel,
            "**What does `[N]` mean?**\n"
            + "- If a number appears before a name, it means the window is extrapolated.\n"
            + "- `[1]` indicates that the previous ToD was missed, `[2]` indicates that the 2 "
            + "previous ToDs were missed, and so on.\n"
            + "- As more extrapolations are done, windows become larger and reliability thus "
            + "decreases.\n"
            + "\n"
            + "**Is there another way to view this data?**\n"
            + "- Yes! See http://edmazur.com/eq (username/password is pinned in #tod).\n"
            + "- Extrapolated windows are also easier to visualize/understand in that UI.\n"
            + "\n"
            + "**How do I avoid being continuously pinged by this channel?**\n"
            + "- Permanently mute it. The bot continously deletes and reposts every minute.");
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private List<DiscordChannel> getChannels() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return List.of(TEST_CHANNEL);
    } else {
      return PROD_CHANNELS;
    }
  }

}
