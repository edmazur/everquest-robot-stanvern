package com.edmazur.eqrs.discord;

import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordServer {

  GOOD_GUYS(1007080931695267870L),
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
