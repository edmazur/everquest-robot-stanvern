package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Duration;
import java.time.Instant;
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

  private static final String TRIGGER = "!lootstatus";

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
        && event.getMessage().getContent().trim().toLowerCase().equals(TRIGGER)) {
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
    int messagesWithoutReactions = 1;
    Message oldestMessageWithoutReaction = null;
    for (Message message : messageSet) {
      if (message.getReactions().isEmpty()) {
        messagesWithoutReactions++;
        if (oldestMessageWithoutReaction == null
            || oldestMessageWithoutReaction.getCreationTimestamp()
                .isAfter(message.getCreationTimestamp())) {
          oldestMessageWithoutReaction = message;
        }
      } else {
        messagesWithReactions++;
      }
    }

    // Report loot status.
    sendReply(
        event,
        String.format(
            "Scanned past %s of messages in <#%d>:\n"
            + "\\- %d total\n"
            + "\\- %d with reactions\n"
            + "\\- %d without reactions "
            + (messagesWithoutReactions > 0 ? "⚠️, oldest: %s" : "✅"),
            LOOKBACK_STRING,
            QUERY_CHANNEL.getId(),
            messagesWithReactions + messagesWithoutReactions,
            messagesWithReactions,
            messagesWithoutReactions,
            oldestMessageWithoutReaction.getLink().toString()));
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
