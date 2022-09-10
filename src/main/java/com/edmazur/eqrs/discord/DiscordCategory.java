package com.edmazur.eqrs.discord;

import java.util.Optional;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordCategory {

  // GG server.
  GG_PHONES(1007149113957171220L),
  GG_IMPORTANT(1007150532919558266L),

  ;

  private final Long id;

  private DiscordCategory(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public boolean isEventChannel(MessageCreateEvent event) {
    return isEventChannel(event.getChannel());
  }

  public boolean isEventChannel(TextChannel channel) {
    Optional<ServerTextChannel> maybeServerTextChannel = channel.asServerTextChannel();
    if (maybeServerTextChannel.isPresent()) {
      Optional<ChannelCategory> maybeChannelCategory = maybeServerTextChannel.get().getCategory();
      if (maybeChannelCategory.isPresent()) {
        return maybeChannelCategory.get().getId() == id;
      }
    }
    return false;
  }

}
