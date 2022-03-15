package com.edmazur.eqrs.discord;

import java.io.File;
import java.util.Optional;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Messageable;
import org.javacord.api.listener.message.MessageCreateListener;

import com.edmazur.eqrs.Config;

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

  public void sendMessage(DiscordChannel discordChannel, String message) {
    getMessageable(discordChannel).sendMessage(message);
  }

  public void sendMessage(DiscordUser discordUser, String message) {
    getMessageable(discordUser).sendMessage(message);
  }

  public void sendMessage(DiscordChannel discordChannel, File image) {
    getMessageable(discordChannel).sendMessage(image);
  }

  public void sendMessage(DiscordUser discordUser, File image) {
    getMessageable(discordUser).sendMessage(image);
  }

  public void sendMessage(DiscordChannel discordChannel, String message, File image) {
    getMessageable(discordChannel).sendMessage(message, image);
  }

  public void sendMessage(DiscordUser discordUser, String message, File image) {
    getMessageable(discordUser).sendMessage(message, image);
  }

  private Messageable getMessageable(DiscordChannel discordChannel) {
    Optional<Channel> maybeChannel =
        discordApi.getChannelById(discordChannel.getId());
    if (maybeChannel.isEmpty()) {
      System.err.println("Could not find channel: " + discordChannel);
    }
    TextChannel channel = maybeChannel.get().asTextChannel().get();
    return channel;
  }

  private Messageable getMessageable(DiscordUser discordUser) {
    return discordApi.getUserById(discordUser.getId()).join();
  }

  public void addListener(MessageCreateListener listener) {
    discordApi.addListener(listener);
  }

}