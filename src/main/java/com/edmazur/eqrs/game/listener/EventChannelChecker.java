package com.edmazur.eqrs.game.listener;

import java.util.function.Predicate;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

public class EventChannelChecker {

  public boolean isAlreadyPosted(String lootCommand, TextChannel textChannel) {
    return !textChannel.getMessagesUntil(new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return message.getContent().toLowerCase().contains(lootCommand.toLowerCase());
      }
    }).join().isEmpty();
  }

}
