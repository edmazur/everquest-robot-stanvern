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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
  private static final List<DiscordChannel> PROD_UNAUDITED_CHANNELS = List.of(
      DiscordChannel.GG_GMOTD,
      DiscordChannel.GG_TICKS_AND_GRATS,
      DiscordChannel.GG_TIMERS,
      DiscordChannel.GG_TOD);

  private static final Map<DiscordCategory, DiscordChannel> TEST_CATEGORY_CHANNEL_MAP =
      Map.ofEntries(
          entry(DiscordCategory.TEST_IMPORATNT, DiscordChannel.TEST_IMPORTANT_AUDIT)
      );
  private static final List<DiscordChannel> TEST_UNAUDITED_CHANNELS = List.of(
      DiscordChannel.TEST_UNAUDITED);

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
    onMessage(
        event.getChannel(),
        event.getMessage().getLastEditTimestamp().get(),
        event.getMessageAuthor().getDisplayName(),
        event.getMessageContent(),
        MessageType.EDIT);
  }

  private void onMessage(
      TextChannel eventChannel,
      Instant instant,
      String author,
      String content,
      MessageType messageType) {
    for (Map.Entry<DiscordCategory, DiscordChannel> mapEntry : getCategoryChannelMap().entrySet()) {
      DiscordCategory discordCategoryToAudit = mapEntry.getKey();
      DiscordChannel discordChannelToWriteAuditLogTo = mapEntry.getValue();
      if (discordCategoryToAudit.isEventChannel(eventChannel)
          && !discordChannelToWriteAuditLogTo.isEventChannel(eventChannel)
          && !DiscordChannel.containsEventChannel(eventChannel, getUnauditedChannels())) {
        Optional<ServerChannel> maybeServerChannel = eventChannel.asServerChannel();
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
        discord.sendMessage(discordChannelToWriteAuditLogTo, auditMessage);
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

  private List<DiscordChannel> getUnauditedChannels() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_UNAUDITED_CHANNELS;
    } else {
      return PROD_UNAUDITED_CHANNELS;
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
