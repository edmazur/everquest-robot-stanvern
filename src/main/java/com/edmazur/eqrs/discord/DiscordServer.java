package com.edmazur.eqrs.discord;

import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordServer {

  CARE(954200812572258314L),
  FORCE_OF_WILL(846213644575572028L),
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
