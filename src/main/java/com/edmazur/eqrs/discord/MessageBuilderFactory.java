package com.edmazur.eqrs.discord;

import org.javacord.api.entity.message.MessageBuilder;

public class MessageBuilderFactory {

  public MessageBuilder create() {
    return new MessageBuilder();
  }

}
