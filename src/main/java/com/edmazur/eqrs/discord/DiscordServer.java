package com.edmazur.eqrs.discord;

import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordServer {

  FORCE_OF_WILL(846213644575572028L),
  TEST(926202321367621652L),
  ;

  private final Long id;

  private DiscordServer(long id) {
    this.id = id;
  }

  public boolean isEventServer(MessageCreateEvent event) {
    return event.getServer().isPresent() && event.getServer().get().getId() == id;
  }

}
