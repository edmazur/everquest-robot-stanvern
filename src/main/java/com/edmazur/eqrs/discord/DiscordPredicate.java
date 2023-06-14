package com.edmazur.eqrs.discord;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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

  public static Predicate<Message> isFromUser(DiscordUser discordUser) {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return message.getAuthor().getId() == discordUser.getId();
      }
    };
  }

  public static Predicate<Message> hasAttachment() {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return !message.getAttachments().isEmpty();
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

  public static Predicate<Message> textMatchesPattern(Pattern pattern) {
    return new Predicate<Message>() {
      @Override
      public boolean test(Message message) {
        return pattern.matcher(message.getContent()).matches();
      }
    };
  }

}
