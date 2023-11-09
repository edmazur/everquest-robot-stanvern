package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.game.Item;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

public class EventChannelMatcher {

  private static final String CATEGORY_PREFIX_CASE_INSENSITIVE = "events";
  private static final String ITEM_NAME_SENTINEL_CASE_INSENSITIVE = "loot table";

  // Keep these in sync.
  private static final Duration EVENT_CHANNEL_MAX_AGE = Duration.ofHours(5);
  private static final String EVENT_CHANNEL_MAX_AGE_STRING = "5 hours";


  private EventChannelMatcher() {
    throw new IllegalStateException("Cannot be instantiated");
  }

  public static ValueOrError<Long> getChannel(EqLogEvent eqLogEvent, List<Item> items) {
    if (items.isEmpty()) {
      return ValueOrError.error("No items found");
    } else if (items.size() > 1) {
      return ValueOrError.error("Multiple items found");
    }
    Item item = items.get(0);

    // Send requests in parallel to get the first message of each relevant event channel.
    List<CompletableFuture<MessageSet>> completableFutures = new ArrayList<>();
    for (ChannelCategory category : Discord.getDiscord().getChannelCategories(
        Discord.getDiscordServer())) {
      if (category.getName().toLowerCase().startsWith(CATEGORY_PREFIX_CASE_INSENSITIVE)) {
        for (Channel channel : category.getChannels()) {
          completableFutures.add(channel.asServerTextChannel().get().getMessagesAfter(1, 0));
        }
      }
    }

    // Collect all responses and put them in reverse timestamp order.
    SortedMap<Instant, Message> messagesInTimestampOrder =
        Maps.newTreeMap(Collections.reverseOrder());
    for (CompletableFuture<MessageSet> completableFuture : completableFutures) {
      Optional<Message> maybeFirstMessage = completableFuture.join().getNewestMessage();
      // This check is needed in case an event channel has no messages.
      if (maybeFirstMessage.isPresent()) {
        Message message = maybeFirstMessage.get();
        Instant eventChannelTimestamp = message.getCreationTimestamp();
        messagesInTimestampOrder.put(eventChannelTimestamp, message);
      }
    }

    // Process the responses in timestamp order to select the first matching candidate.
    Pattern itemNamePattern = item.getNamePattern();
    Instant gratsTimestamp = eqLogEvent.getTimestamp()
        .atZone(ZoneId.of(Config.getConfig().getString(Config.Property.TIMEZONE_GAME))).toInstant();
    Map<String, String> lootGroupExpansions = LootGroupExpander.getExpansions();
    for (Map.Entry<Instant, Message> mapEntry : messagesInTimestampOrder.entrySet()) {
      final Instant eventChannelTimestamp = mapEntry.getKey();
      final Message message = mapEntry.getValue();
      String contentLower = message.getContent().toLowerCase();
      for (Map.Entry<String, String> mapEntry2 : lootGroupExpansions.entrySet()) {
        final String lootGroupLower = mapEntry2.getKey().toLowerCase();
        final String expansion = mapEntry2.getValue();
        contentLower = contentLower.replace(lootGroupLower, expansion);
      }
      if (contentLower.contains(ITEM_NAME_SENTINEL_CASE_INSENSITIVE)
          && itemNamePattern.matcher(contentLower).matches()
          && gratsTimestamp.isAfter(eventChannelTimestamp)) {
        long channelId = message.getChannel().getId();
        if (eventChannelTimestamp.plus(EVENT_CHANNEL_MAX_AGE).isBefore(gratsTimestamp)) {
          return ValueOrError.error(String.format(
              "Matched <#%d>, but ignoring because it's more than %s old",
              channelId, EVENT_CHANNEL_MAX_AGE_STRING));
        } else {
          return ValueOrError.value(channelId);
        }
      }
    }

    return ValueOrError.error("Item not found in any event channel's loot table");
  }
}
