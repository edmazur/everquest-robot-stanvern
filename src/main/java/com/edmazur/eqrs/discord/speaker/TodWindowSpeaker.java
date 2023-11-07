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


  public TodWindowSpeaker() { }

  @Override
  public void run() {
    try {
      Table table = RaidTargetTableMaker.make();
      Discord.getDiscord().deleteMessagesMatchingPredicate(
          getChannel(), DiscordPredicate.isFromYourself());
      for (String messages : DiscordTableFormatter.getMessages(table, Map.of(0, 1))) {
        // Wait for the Future to complete before sending the next message. In testing, not having
        // this in place led to out-of-order messages when they got sent in rapid succession.
        Discord.getDiscord().sendMessage(getChannel(), messages).join();
      }
      Discord.getDiscord().sendMessage(getChannel(),
          "**What does `[N]` mean?**\n"
          + "\\- If a number appears before a name, it means the window is extrapolated.\n"
          + "\\- `[1]` indicates that the previous ToD was missed, `[2]` indicates that the 2 "
          + "previous ToDs were missed, and so on.\n"
          + "\\- As more extrapolations are done, windows become larger and reliability thus "
          + "decreases.\n"
          + "\n"
          + "**Is there another way to view this data?**\n"
          + "\\- Yes! See http://edmazur.com/eq (username/password is pinned in #tod).\n"
          + "\\- Extrapolated windows are also easier to visualize/understand in that UI.\n"
          + "\n"
          + "**How do I avoid being continuously pinged by this channel?**\n"
          + "\\- Permanently mute it. The bot continously deletes and reposts every minute.\n"
          + "\n"
          + "**What about features XYZ - where can I send feedback?**\n"
          + "\\- I (Stanvern) am **very** open to feedback! My goal is to streamline ToD/windows "
          + "as much as possible, so if you have any feedback at all, positive or negative, I "
          + "really want to hear it. Feel free to reach out to me (GG Discord, DM, in-game, etc.) "
          + "as much as you want.");
    } catch (Throwable t) {
      t.printStackTrace();
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
