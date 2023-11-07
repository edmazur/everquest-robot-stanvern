package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.game.CharInfo;
import com.edmazur.eqrs.game.CharInfoScraper;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class CharInfoScreenshotListener implements MessageCreateListener {

  private static final Logger LOGGER = new Logger();
  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_BOT_CAMP;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_BOT_SCRAPE;
  private static final Predicate<Message> PREDICATE = DiscordPredicate.hasImage();

  private static final File SUCCESS_IMAGE = new File("src/main/resources/str.png");

  private final CharInfoScraper charInfoScraper;

  public CharInfoScreenshotListener() {
    this.charInfoScraper = new CharInfoScraper();
    LOGGER.log("%s running", this.getClass().getName());
    for (Message message : Discord.getDiscord().getUnrepliedMessagesMatchingPredicate(
        getChannel(), PREDICATE)) {
      handle(message);
    }
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (getChannel().isEventChannel(event) && PREDICATE.test(event.getMessage())) {
      handle(event.getMessage());
    }
  }

  private void handle(Message message) {
    // This can be kind of slow, so run it in a new thread so you don't block other listeners.
    new Thread(() -> {
      File image = null;
      try {
        image = File.createTempFile(this.getClass().getName() + "-", ".png");
        List<MessageAttachment> messageAttachments = message.getAttachments();
        for (MessageAttachment messageAttachment : messageAttachments) {
          if (messageAttachment.isImage()) {
            ImageIO.write(messageAttachment.asImage().join(), "png", image);
            break;
          }
        }
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return;
      }

      CharInfo charInfo = charInfoScraper.scrape(image);
      boolean hasAnyFieldScraped =
          charInfo.hasName()
          || charInfo.hasEqClass()
          || charInfo.hasLevel()
          || charInfo.hasExpPercentToNextLevel();
      if (hasAnyFieldScraped) {
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(Color.GREEN)
            .setThumbnail(SUCCESS_IMAGE)
            .addField("Char info parsed:",
                  "` Name:` " + (charInfo.hasName() ? charInfo.getName() : "?") + "\n"
                + "`Class:` " + (charInfo.hasEqClass() ? charInfo.getEqClass() : "?") + "\n"
                + "`Level:` " + (charInfo.hasLevel() ? charInfo.getLevel() : "?") + "\n"
                + "`  Exp:` " + (charInfo.hasExpPercentToNextLevel()
                    ? charInfo.getExpPercentToNextLevel() + "%" : "?") + "\n");
        new MessageBuilder()
            .replyTo(message)
            .setAllowedMentions(new AllowedMentionsBuilder().build())
            .setEmbed(embed)
            .send(message.getChannel());
      }
    }).start();
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().getBoolean(Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
