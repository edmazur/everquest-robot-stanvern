package com.edmazur.eqrs.discord;

import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordUser {

  EDMAZUR(616823653463621654L),
  ;

  private final Long id;

  private DiscordUser(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public boolean isEventUser(MessageCreateEvent event) {
    return event.getMessageAuthor().getId() == id;
  }

}
