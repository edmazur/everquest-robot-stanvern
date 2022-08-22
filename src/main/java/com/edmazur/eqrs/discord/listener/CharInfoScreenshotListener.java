package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
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
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class CharInfoScreenshotListener implements MessageCreateListener {

  // TODO: Either delete all the OCR stuff or permanently enable it. Currently doing this half-in,
  // half-out approach while setting up new lightweight server. Once it's stable, temporarily
  // re-enable the OCR stuff and see if the server can handle the load.
  private static final boolean ENABLE = false;

  private static final List<DiscordChannel> CHANNELS = List.of(
      DiscordChannel.FOW_BOT_BOOT_CAMP,
      DiscordChannel.FOW_BOT_SCREAMING_ROOM);
  private static final Predicate<Message> PREDICATE = DiscordPredicate.hasImage();

  private static final File SUCCESS_IMAGE = new File("src/main/resources/str.png");

  private Config config;
  private Discord discord;
  private CharInfoScraper charInfoScraper;

  public CharInfoScreenshotListener(
      Config config,
      Discord discord,
      CharInfoScraper charInfoScraper) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.charInfoScraper = charInfoScraper;
  }

  public void init() {
    if (!ENABLE) {
      return;
    }

    for (DiscordChannel discordChannel : getChannelsToReadFrom()) {
      for (Message message :
          discord.getUnrepliedMessagesMatchingPredicate(discordChannel, PREDICATE)) {
        handle(message);
      }
    }
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (!ENABLE) {
      return;
    }

    if (DiscordChannel.containsEventChannel(event, getChannelsToReadFrom())
        && PREDICATE.test(event.getMessage())) {
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
            ImageIO.write(messageAttachment.downloadAsImage().join(), "png", image);
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
        message.reply(embed);
      }
    }).start();
  }

  private List<DiscordChannel> getChannelsToReadFrom() {
    if (config.getBoolean(Property.DEBUG)) {
      return List.of(DiscordChannel.TEST_GENERAL);
    } else {
      return CHANNELS;
    }
  }

}
