package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordServer;
import com.edmazur.eqrs.game.Item;
import com.google.common.collect.Maps;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

public class EventChannelMatcher {

  private static final String CATEGORY_PREFIX_CASE_INSENSITIVE = "events";
  private static final String ITEM_NAME_SENTINEL_CASE_INSENSITIVE = "loot table";

  private Config config;
  private Discord discord;

  public EventChannelMatcher(Config config, Discord discord) {
    this.config = config;
    this.discord = discord;
  }

  public Optional<Channel> getChannel(EqLogEvent eqLogEvent, Item item) {
    // Send requests in parallel to get the first message of each relevant event channel.
    List<CompletableFuture<MessageSet>> completableFutures = new ArrayList<>();
    for (ChannelCategory category : discord.getChannelCategories(getServer())) {
      if (category.getName().toLowerCase().startsWith(CATEGORY_PREFIX_CASE_INSENSITIVE)) {
        for (Channel channel : category.getChannels()) {
          completableFutures.add(channel.asServerTextChannel().get().getMessagesAfter(1, 0));
        }
      }
    }

    // Collect all responses and put them in timestamp order.
    SortedMap<Instant, Message> messagesInTimestampOrder = Maps.newTreeMap();
    for (CompletableFuture<MessageSet> completableFuture : completableFutures) {
      Message message = completableFuture.join().getNewestMessage().get();
      Instant eventChannelTimestamp = message.getCreationTimestamp();
      messagesInTimestampOrder.put(eventChannelTimestamp, message);
    }

    // Process the responses in timestamp order to select the first matching candidate.
    String itemNameLower = item.getName().toLowerCase();
    Instant gratsTimestamp = eqLogEvent.getTimestamp()
        .atZone(ZoneId.of(config.getString(Config.Property.TIMEZONE_GAME))).toInstant();
    for (Map.Entry<Instant, Message> mapEntry : messagesInTimestampOrder.entrySet()) {
      Instant eventChannelTimestamp = mapEntry.getKey();
      Message message = mapEntry.getValue();
      String contentLower = message.getContent().toLowerCase();
      if (contentLower.contains(ITEM_NAME_SENTINEL_CASE_INSENSITIVE)
          && contentLower.contains(itemNameLower)
          && gratsTimestamp.isAfter(eventChannelTimestamp)) {
        return Optional.of(message.getChannel());
      }
    }

    return Optional.empty();
  }

  private DiscordServer getServer() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return DiscordServer.TEST;
    } else {
      return DiscordServer.GOOD_GUYS;
    }
  }

}
