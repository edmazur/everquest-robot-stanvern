package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.discord.DiscordTableFormatter;
import com.edmazur.eqrs.game.RaidTargetTableMaker;
import com.edmazur.eqrs.table.Table;
import java.util.Map;

public class TodWindowSpeaker implements Runnable {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TIMERS;
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
      discord.deleteMessagesMatchingPredicate(getChannel(), DiscordPredicate.isFromYourself());
      for (String messages : discordTableFormatter.getMessages(table, Map.of(0, 1))) {
        // Wait for the Future to complete before sending the next message. In testing, not having
        // this in place led to out-of-order messages when they got sent in rapid succession.
        discord.sendMessage(getChannel(), messages).join();
      }
      discord.sendMessage(getChannel(),
          "**What does `[N]` mean?**\n"
          + "\\- If a number appears before a name, it means the window is extrapolated.\n"
          + "\\- `[1]` indicates that the previous ToD was missed, `[2]` indicates that the 2 "
          + "previous ToDs were missed, and so on.\n"
          + "\\- As more extrapolations are done, windows become larger and reliability thus "
          + "decreases.\n"
          + "\n"
          + "**Is there another way to view this data?**\n"
          + "\\- Yes! See <http://edmazur.com/eq> (username/password is pinned in #tod).\n"
          + "\\- Extrapolated windows are also easier to visualize/understand in that UI.\n"
          + "\n"
          + "**How do I avoid being continuously pinged by this channel?**\n"
          + "\\- Permanently mute it. The bot continously deletes and reposts every minute.\n"
          + "\n"
          + "**What about features XYZ - where can I send feedback?**\n"
          + "\\- I (Stanvern) am **very** open to feedback! My goal is to streamline ToD/windows "
          + "as much as possible, so if you have any feedback at all, positive or negative, I "
          + "really want to hear it. Feel free to reach out to me (GG Discord, DM, in-game, etc.) "
          + "as much as you want.\n"
          + "\n"
          + "**Where can I learn more about this bot, run it for Quarm, etc.?**\n"
          + "\\- Check out the FAQ: <https://github.com/edmazur/everquest-robot-stanvern#faq>");
    } catch (Throwable t) {
      t.printStackTrace();
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
