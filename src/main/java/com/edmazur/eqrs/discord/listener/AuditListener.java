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
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
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
          entry(DiscordCategory.TEST_IMPORTANT, DiscordChannel.TEST_IMPORTANT_AUDIT)
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


  public AuditListener() {
    Discord.getDiscord().addListener(this);
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    onMessage(
        event.getChannel(),
        event.getMessage().getCreationTimestamp(),
        event.getMessageAuthor(),
        event.getMessageContent(),
        MessageType.CREATE);
  }

  @Override
  public void onMessageEdit(MessageEditEvent event) {
    // Per https://discord.com/channels/151037561152733184/151326093482262528/1116845429682864199,
    // more than "true" edit events can trigger MessageEditEvent, e.g. embed generation. As a
    // workaround for this, inspect whether the last edit timestamp is present to determine whether
    // or not this is a "true" edit event.
    Optional<Instant> maybeLastEditTimestamp = event.getMessage().getLastEditTimestamp();
    if (maybeLastEditTimestamp.isEmpty()) {
      return;
    }

    onMessage(
        event.getChannel(),
        maybeLastEditTimestamp.get(),
        event.getMessageAuthor(),
        event.getMessageContent(),
        MessageType.EDIT);
  }

  private void onMessage(
      TextChannel eventChannel,
      Instant instant,
      MessageAuthor author,
      String content,
      MessageType messageType) {
    for (Map.Entry<DiscordCategory, DiscordChannel> mapEntry : getCategoryChannelMap().entrySet()) {
      DiscordCategory discordCategoryToAudit = mapEntry.getKey();
      DiscordChannel discordChannelToWriteAuditLogTo = mapEntry.getValue();
      if (discordCategoryToAudit.isEventChannel(eventChannel)
          && !discordChannelToWriteAuditLogTo.isEventChannel(eventChannel)
          && !DiscordChannel.containsEventChannel(eventChannel, getUnauditedChannels())) {
        // TODO: Use an "embed" with nice colors/images for different channels.
        String auditMessage = String.format("**%s** message from <@%d> in <#%s> at **%s**:\n%s",
            messageType.getDescription(),
            author.getId(),
            eventChannel.getId(),
            instant.atZone(ZoneId.of(Config.getConfig().getString(Property.TIMEZONE_GUILD)))
                .format(DateTimeFormatter.RFC_1123_DATE_TIME),
            content);
        Discord.getDiscord().sendMessage(
            discordChannelToWriteAuditLogTo,
            new MessageBuilder()
                .setAllowedMentions(new AllowedMentionsBuilder().build())
                .setContent(auditMessage));
        break;
      }
    }
  }

  private Map<DiscordCategory, DiscordChannel> getCategoryChannelMap() {
    if (Config.getConfig().isDebug()) {
      return TEST_CATEGORY_CHANNEL_MAP;
    } else {
      return PROD_CATEGORY_CHANNEL_MAP;
    }
  }

  private List<DiscordChannel> getUnauditedChannels() {
    if (Config.getConfig().isDebug()) {
      return TEST_UNAUDITED_CHANNELS;
    } else {
      return PROD_UNAUDITED_CHANNELS;
    }
  }

}
