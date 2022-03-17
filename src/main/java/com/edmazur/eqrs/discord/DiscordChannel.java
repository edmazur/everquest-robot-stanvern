package com.edmazur.eqrs.discord;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordChannel {

  // Force of Will server.
  AFTERHOURS_BATPHONE(869043670509314158L),
  ANNOUNCEMENT_AUDIT(883072225937346590L),
  GUILD_ANNOUNCEMENTS(846969203603800134L),
  GUILD_CHAT(846989332878852117L),
  RAID_BATPHONE(846969268948303872L),
  RAIDER_GMOTD(952948725569966100L),
  RAIDER_CHAT(919595636796583976L),
  TOD(846993698662252604L),

  // Personal test server.
  // TODO: This is probably more appropriate as a config setting, but this being
  // an enum makes that a bit complicated.
  ROBOT_STANVERN_TESTING(953825205820731443L),
  ;

  private final Long id;

  private DiscordChannel(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public boolean isEventChannel(MessageCreateEvent event) {
    return isEventChannel(event.getChannel());
  }

  public boolean isEventChannel(TextChannel channel) {
    return channel.getId() == id;
  }

}