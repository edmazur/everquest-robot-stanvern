package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class Discord {

  private final DiscordApi discordApi;

  public Discord(Config config) {
    discordApi = new DiscordApiBuilder()
        .setToken(config.getString(Config.Property.DISCORD_PRIVATE_KEY))
        .login()
        .join();

    // TODO: Randomly update this every so often and cycle through a bunch of fun ones.
    discordApi.updateActivity(ActivityType.WATCHING, "everything, always");
  }

  public CompletableFuture<Message> sendMessage(
      DiscordChannel discordChannel, String message) {
    return getTextChannel(discordChannel)
        .sendMessage(message)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordUser discordUser, String message) {
    return getUser(discordUser)
        .sendMessage(message)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordChannel discordChannel, File image) {
    return getTextChannel(discordChannel)
        .sendMessage(image)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordUser discordUser, File image) {
    return getUser(discordUser)
        .sendMessage(image)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordChannel discordChannel, String message, File image) {
    return getTextChannel(discordChannel)
        .sendMessage(message, image)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordUser discordUser, String message, File image) {
    return getUser(discordUser)
        .sendMessage(message, image)
        .exceptionally(ExceptionLogger.get());
  }

  public CompletableFuture<Message> sendMessage(
      DiscordChannel discordChannel, MessageBuilder messageBuilder) {
    return messageBuilder.send(getTextChannel(discordChannel));
  }

  public Optional<String> getLastMessageMatchingPredicate(
      DiscordChannel discordChannel,
      Predicate<Message> predicate) {
    Optional<Message> maybeMessage = getTextChannel(discordChannel)
        .getMessagesUntil(predicate).join()
        .getOldestMessage();
    if (maybeMessage.isEmpty()) {
      return Optional.empty();
    }
    Message message = maybeMessage.get();
    return Optional.of(message.getContent());
  }

  /**
   * Gets messages from a channel matching the given predicate that haven't been replied to by the
   * bot, sorted by oldest first. Intended purpose is for checking on startup for messages that were
   * missed while bot was offline.
   *
   * @param discordChannel The Discord channel to search in.
   * @param predicate The predicate to check with.
   * @return The list of unreplied messages.
   */
  public List<Message> getUnrepliedMessagesMatchingPredicate(
      DiscordChannel discordChannel,
      Predicate<Message> predicate) {
    List<Message> unrepliedMessages = new ArrayList<>();
    Set<Long> messagesWithReplies = new HashSet<>();
    Predicate<Message> replyPredicate =
        DiscordPredicate.isFromYourself().and(DiscordPredicate.isReply());
    Iterator<Message> iterator = getTextChannel(discordChannel).getMessagesAsStream().iterator();
    while (iterator.hasNext()) {
      Message message = iterator.next();

      // Record messages that have been replied to by you.
      if (replyPredicate.test(message)) {
        messagesWithReplies.add(message.getReferencedMessage().get().getId());
        continue;
      }

      // If this is a message that has been replied to by you, then the search is done.
      if (messagesWithReplies.contains(message.getId())) {
        break;
      }

      // If you get to this point, then the message is unreplied to. If it also matches the
      // predicate, then add it to the list to be returned.
      if (predicate.test(message)) {
        unrepliedMessages.add(message);
      }
    }
    // Reverse the list to be returned so that oldest messages are first.
    Collections.reverse(unrepliedMessages);
    return unrepliedMessages;
  }

  public void deleteMessagesMatchingPredicate(
      DiscordChannel discordChannel,
      Predicate<Message> predicate) {
    getTextChannel(discordChannel).getMessagesAsStream()
        .filter(predicate)
        .forEach(message -> message.delete());
  }

  private TextChannel getTextChannel(DiscordChannel discordChannel) {
    Optional<Channel> maybeChannel = discordApi.getChannelById(discordChannel.getId());
    if (maybeChannel.isEmpty()) {
      // TODO: Handle this more gracefully. The main way this can get triggered is if a Discord
      // channel currently in use by the bot suddenly gets deleted, which should be fairly rare.
      throw new IllegalStateException("Could not find channel: " + discordChannel);
    }
    TextChannel channel = maybeChannel.get().asTextChannel().get();
    return channel;
  }

  private User getUser(DiscordUser discordUser) {
    return discordApi.getUserById(discordUser.getId()).join();
  }

  public void addListener(MessageCreateListener listener) {
    discordApi.addListener(listener);
  }

}
