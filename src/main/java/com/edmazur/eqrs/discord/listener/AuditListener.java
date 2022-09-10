package com.edmazur.eqrs.discord.listener;

import static java.util.Map.entry;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordCategory;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

public class AuditListener implements MessageCreateListener, MessageEditListener {

  private static final Logger LOGGER = new Logger();

  private static final Map<DiscordCategory, DiscordChannel> PROD_CATEGORY_CHANNEL_MAP =
      Map.ofEntries(
          entry(DiscordCategory.GG_PHONES, DiscordChannel.GG_PHONE_AUDIT),
          entry(DiscordCategory.GG_IMPORTANT, DiscordChannel.GG_IMPORTANT_AUDIT)
      );

  private static final Map<DiscordCategory, DiscordChannel> TEST_CATEGORY_CHANNEL_MAP =
      Map.ofEntries(
          entry(DiscordCategory.TEST_IMPORATNT, DiscordChannel.TEST_IMPORTANT_AUDIT)
      );

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

  public AuditListener(Config config, Discord discord) {
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
    for (Map.Entry<DiscordCategory, DiscordChannel> mapEntry : getCategoryChannelMap().entrySet()) {
      DiscordCategory discordCategory = mapEntry.getKey();
      DiscordChannel discordChannel = mapEntry.getValue();
      if (discordCategory.isEventChannel(channel) && !discordChannel.isEventChannel(channel)) {
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
            stripMentions(content));
        discord.sendMessage(discordChannel, auditMessage);
        break;
      }
    }
  }

  private Map<DiscordCategory, DiscordChannel> getCategoryChannelMap() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CATEGORY_CHANNEL_MAP;
    } else {
      return PROD_CATEGORY_CHANNEL_MAP;
    }
  }

  /*
   * Strips all mentions (@here, @everyone, custom roles, etc.) from the content.
   */
  private String stripMentions(String content) {
    // TODO: Make this more robust by avoiding stripping from non-role usage of @. I assume there's
    // some way to inspect the message to differentiate something like "@everyone hi" vs. something
    // like "blah blah random @ blah blah" (couldn't think of a good example for the non-role usage,
    // hence this being a TODO for an edge case that'll probably be very rare).
    return content.replace("@", "@ ");
  }

}
