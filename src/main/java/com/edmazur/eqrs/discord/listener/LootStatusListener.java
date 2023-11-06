package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class LootStatusListener implements MessageCreateListener {

  static final String TRIGGER = "!lootstatus";

  private static final int MAX_LINKS = 10;
  private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

  private static final DiscordChannel PROD_COMMAND_CHANNEL = DiscordChannel.GG_GROUP_TEXT;
  private static final DiscordChannel TEST_COMMAND_CHANNEL = DiscordChannel.TEST_GENERAL;
  private static final DiscordChannel QUERY_CHANNEL = DiscordChannel.GG_TICKS_AND_GRATS;

  // Keep these in sync.
  private static final Duration LOOKBACK = Duration.ofDays(7);
  private static final String LOOKBACK_STRING = "7 days";

  private Config config;
  private Discord discord;

  public LootStatusListener(Config config, Discord discord) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (getChannel().isEventChannel(event)
        && event.getMessage().getContent().toLowerCase().contains(TRIGGER)) {
      reportLootStatus(event);
    }
  }

  private void reportLootStatus(MessageCreateEvent event) {
    Optional<MessageSet> maybeMessageSet = discord.getMessagesWithinPast(QUERY_CHANNEL, LOOKBACK);
    if (maybeMessageSet.isEmpty()) {
      sendReply(event, String.format("Error getting messages from <#%d>", QUERY_CHANNEL.getId()));
      return;
    }
    MessageSet messageSet = maybeMessageSet.get();

    // Put messages in order. This may not be necessary based on observed Javacord behavior, but the
    // API definitions don't seem to make any promises about ordering. This should be fast enough.
    Map<Instant, Message> messagesInOrder = new TreeMap<Instant, Message>();
    for (Message message : messageSet) {
      messagesInOrder.put(message.getCreationTimestamp(), message);
    }

    // Collect stats.
    int messagesWithReactions = 0;
    List<Message> messagesWithoutReactions = Lists.newArrayList();
    for (Message message : messageSet) {
      if (message.getReactions().isEmpty()) {
        messagesWithoutReactions.add(message);
      } else {
        messagesWithReactions++;
      }
    }

    // Report loot status.
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            "Scanned past %s of messages in <#%d>:\n",
            LOOKBACK_STRING,
            QUERY_CHANNEL.getId()));
    sb.append(messagesWithoutReactions.isEmpty() ? ":white_check_mark:" : "⚠️");
    sb.append(
        String.format(
            " `%d total, %d with reactions, %d without reactions`",
            messagesWithReactions + messagesWithoutReactions.size(),
            messagesWithReactions,
            messagesWithoutReactions.size()));
    if (!messagesWithoutReactions.isEmpty()) {
      sb.append("\n");
      sb.append(String.format("First without reactions (limited to %d):\n", MAX_LINKS));
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter
          .ofPattern(DATE_TIME_FORMAT)
          .withZone(ZoneId.of(config.getString(Property.TIMEZONE_GUILD)));
      for (int i = 0; i < Math.min(MAX_LINKS, messagesWithoutReactions.size()); i++) {
        Message messageWithoutReactions = messagesWithoutReactions.get(i);
        sb.append(
            String.format(
                "%d. %s (ET: %s - <t:%d:R>)\n",
                i + 1,
                messageWithoutReactions.getLink().toString(),
                dateTimeFormatter.format(messageWithoutReactions.getCreationTimestamp()),
                messageWithoutReactions.getCreationTimestamp().toEpochMilli() / 1000));
      }
    }
    sendReply(event, sb.toString());
  }

  private void sendReply(MessageCreateEvent event, String content) {
    new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .setContent(content)
        .send(event.getChannel());
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Property.DEBUG)) {
      return TEST_COMMAND_CHANNEL;
    } else {
      return PROD_COMMAND_CHANNEL;
    }
  }

}
