package com.edmazur.eqrs.discord;

import java.util.List;
import java.util.function.Predicate;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

public class DiscordPredicate {

  public static Predicate<Message> hasImage() {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        List<MessageAttachment> messageAttachments = message.getAttachments();
        for (MessageAttachment messageAttachment : messageAttachments) {
          if (messageAttachment.isImage()) {
            return true;
          }
        }
        return false;
      }
    };
  }

  public static Predicate<Message> isFromYourself() {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return message.getAuthor().isYourself();
      }
    };
  }

  public static Predicate<Message> isReply() {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return message.getReferencedMessage().isPresent();
      }
    };
  }

}
