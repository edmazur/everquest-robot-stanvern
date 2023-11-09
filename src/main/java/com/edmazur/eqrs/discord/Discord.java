package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.commands.RaidTargetCommand;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import me.s3ns3iw00.jcommands.CommandHandler;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import org.javacord.api.util.logging.ExceptionLogger;

public class Discord {

  private static final Logger LOGGER = new Logger();
  private final DiscordApi discordApi;
  private static Discord discord;

  public static Discord getDiscord() {
    if (discord == null) {
      discord = new Discord();
    }
    return discord;
  }

  private Discord() {
    discordApi = new DiscordApiBuilder()
        .setToken(Config.getConfig().getString(Config.Property.DISCORD_PRIVATE_KEY))
        .addIntents(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS)
        .login()
        .join();

    // Register all slash-commands
    registerCommands();

    // TODO: Randomly update this every so often and cycle through a bunch of fun ones.
    discordApi.updateActivity(ActivityType.WATCHING, "everything, always");
  }

  private void registerCommands() {
    CommandHandler.setApi(discordApi);

    Server server = getServer();

    // RaidTarget subscription command
    CommandHandler.registerCommand(new RaidTargetCommand(), server);
  }

  public static DiscordServer getDiscordServer() {
    if (Config.getConfig().isDebug()) {
      return DiscordServer.TEST;
    }
    return DiscordServer.GOOD_GUYS;
  }

  private Server getServer() {
    // Determine which server we're using
    if (Config.getConfig().isDebug()) {
      return discordApi.getServerById(DiscordServer.TEST.getId()).orElse(null);
    } else {
      return discordApi.getServerById(DiscordServer.GOOD_GUYS.getId()).orElse(null);
    }
  }

  public boolean isUserAuthorized(long userId, DiscordRole requiredRole) {
    Server server = getServer();
    User discordUser = getUser(userId);
    Role discordRole = server.getRoleById(requiredRole.getForServer(getDiscordServer()).getId())
        .orElse(null);
    List<Role> roleList = discordUser.getRoles(server);
    return roleList.contains(discordRole);
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
      long discordUserId, String message) {
    return getUser(discordUserId)
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

  public Optional<Message> getLastMessageMatchingPredicate(
      DiscordChannel discordChannel,
      Predicate<Message> predicate) {
    return Optional.ofNullable(
        getTextChannel(discordChannel)
            .getMessagesUntil(predicate)
            .join()
            .getOldestMessage()
            .get());
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

  public Collection<ChannelCategory> getChannelCategories(DiscordServer discordServer) {
    Optional<Server> maybeServer = discordApi.getServerById(discordServer.getId());
    if (maybeServer.isEmpty()) {
      System.err.println(
          "Returning empty channel category list, could not find server: " + discordServer);
      return Collections.emptyList();
    }
    return maybeServer.get().getChannelCategories();
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

  // TODO: This is kind of hacky. It's being used to expose Javacord internals outside of this class
  // to be able to send messages to one-off channels (as opposed to ones defined in the
  // DiscordChannel enum). To get around this, you should probably change DiscordChannel to be an
  // extensible enum (https://stackoverflow.com/a/1414896/192236).
  public TextChannel getTextChannel(long id) {
    Optional<Channel> maybeChannel = discordApi.getChannelById(id);
    if (maybeChannel.isEmpty()) {
      throw new IllegalStateException("Could not find channel: " + id);
    }
    TextChannel channel = maybeChannel.get().asTextChannel().get();
    return channel;
  }

  public boolean hasAnyRole(User user, List<DiscordRole> roles, Server server) {
    for (Role userRole : user.getRoles(server)) {
      for (DiscordRole roleLookingFor : roles) {
        if (userRole.getId() == roleLookingFor.getId()) {
          return true;
        }
      }
    }
    return false;
  }

  public CompletableFuture<Message> getMessage(long id, TextChannel textChannel) {
    return discordApi.getMessageById(id, textChannel);
  }

  public Optional<MessageSet> getMessagesWithinPast(
      DiscordChannel discordChannel,
      Duration duration) {
    try {
      return Optional.of(getTextChannel(discordChannel)
          .getMessagesUntil(DiscordPredicate.isOlderThan(duration)).get());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<MessageSet> getMessagesBetween(
      DiscordChannel discordChannel,
      Instant start,
      Instant end) {
    try {
      return Optional.of(getTextChannel(discordChannel)
          .getMessagesBetween(getSnowflake(start), getSnowflake(end)).get());
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  private User getUser(DiscordUser discordUser) {
    return discordApi.getUserById(discordUser.getId()).join();
  }

  private User getUser(long discordUserId) {
    return discordApi.getUserById(discordUserId).join();
  }

  public void addListener(MessageCreateListener listener) {
    discordApi.addListener(listener);
  }

  public void addListener(ButtonClickListener listener) {
    discordApi.addListener(listener);
  }

  public void addListener(ReactionAddListener listener) {
    discordApi.addListener(listener);
  }

  public void addListener(ReactionRemoveAllListener listener) {
    discordApi.addListener(listener);
  }

  public void addListener(ReactionRemoveListener listener) {
    discordApi.addListener(listener);
  }

  private static long getSnowflake(Instant instant) {
    return (instant.toEpochMilli() - 1420070400000L) << 22;
  }

}
