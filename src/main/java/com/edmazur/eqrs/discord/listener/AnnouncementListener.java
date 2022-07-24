package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

public class AnnouncementListener implements MessageCreateListener, MessageEditListener {

  private static final Logger LOGGER = new Logger();

  private static final List<DiscordChannel> PROD_CHANNELS_TO_READ_FROM = Arrays.asList(
      DiscordChannel.FOW_RAID_BATPHONE,
      DiscordChannel.FOW_AFTERHOURS_BATPHONE,
      DiscordChannel.FOW_GUILD_ANNOUNCEMENTS);

  private static final List<DiscordChannel> TEST_CHANNELS_TO_READ_FROM = Arrays.asList(
      DiscordChannel.TEST_BATPHONE,
      DiscordChannel.TEST_ANNOUNCEMENTS);

  private static final DiscordChannel PROD_CHANNEL_TO_WRITE_TO =
      DiscordChannel.FOW_ANNOUNCEMENT_AUDIT;

  private static final DiscordChannel TEST_CHANNEL_TO_WRITE_TO =
      DiscordChannel.TEST_ANNOUNCEMENT_AUDIT;

  private enum MessageType {
    CREATE("New"),
    EDIT("Edited"),
    ;

    private final String description;

    private MessageType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  private final Config config;
  private final Discord discord;

  public AnnouncementListener(Config config, Discord discord) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    onMessage(
        event.getChannel(),
        event.getMessage().getCreationTimestamp(),
        event.getMessageAuthor().getDisplayName(),
        event.getMessageContent(),
        MessageType.CREATE);
  }

  @Override
  public void onMessageEdit(MessageEditEvent event) {
    try {
      onMessage(
          event.getChannel(),
          event.requestMessage().get().getLastEditTimestamp().get(),
          event.getMessageAuthor().isPresent()
              ? event.getMessageAuthor().get().getDisplayName() : "",
          event.getNewContent(),
          MessageType.EDIT);
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void onMessage(
      TextChannel channel,
      Instant instant,
      String author,
      String content,
      MessageType messageType) {
    boolean isChannelToReadFrom = false;
    if (config.getBoolean(Property.DEBUG)) {
      isChannelToReadFrom = DiscordChannel.TEST_BATPHONE.isEventChannel(channel);
    } else {
      for (DiscordChannel channelToReadFrom : getChannelsToReadFrom()) {
        if (channelToReadFrom.isEventChannel(channel)) {
          isChannelToReadFrom = true;
          break;
        }
      }
    }

    if (isChannelToReadFrom) {
      Optional<ServerChannel> maybeServerChannel = channel.asServerChannel();
      if (maybeServerChannel.isEmpty()) {
        return;
      }
      // TODO: Make the #channel linked.
      // TODO: Maybe make the user linked.
      // TODO: Use an "embed" with nice colors/images for different channels.
      String auditMessage = String.format("**%s** message from **%s** in **#%s** at **%s**:\n%s",
          messageType.getDescription(),
          author,
          maybeServerChannel.get().getName(),
          instant.atZone(ZoneId.of(config.getString(Property.TIMEZONE_GUILD)))
              .format(DateTimeFormatter.RFC_1123_DATE_TIME),
          content);
      discord.sendMessage(getChannelToWriteTo(), auditMessage);
    }
  }

  private List<DiscordChannel> getChannelsToReadFrom() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNELS_TO_READ_FROM;
    } else {
      return PROD_CHANNELS_TO_READ_FROM;
    }
  }

  private DiscordChannel getChannelToWriteTo() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL_TO_WRITE_TO;
    } else {
      return PROD_CHANNEL_TO_WRITE_TO;
    }
  }

}
