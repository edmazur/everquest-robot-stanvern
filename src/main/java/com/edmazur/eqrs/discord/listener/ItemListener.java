package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordServer;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ItemListener implements MessageCreateListener {

  private static final Logger LOGGER = new Logger();
  private static final String TRIGGER = "!item";


  public ItemListener() {
    Discord.getDiscord().addListener(this);
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (Config.getConfig().getBoolean(Property.DEBUG) && !DiscordServer.TEST.isEventServer(event)) {
      // In debug mode, ignore non-test server messages.
      return;
    } else if (!Config.getConfig().getBoolean(Property.DEBUG)
        && DiscordServer.TEST.isEventServer(event)) {
      // In non-debug mode, ignore test server messages.
      return;
    }

    // Ignore yourself.
    if (event.getMessageAuthor().isYourself()) {
      return;
    }

    // Ignore messages without trigger.
    if (!event.getMessageContent().contains(TRIGGER)) {
      return;
    }

    event.getChannel().type();
    List<Item> items = ItemDatabase.getItemDatabase().parse(event.getMessageContent());
    if (items.isEmpty()) {
      event.getMessage().reply("Sorry, saw !item request, but couldn't match any item names. "
          + "Search is case-insensitive, but partial matches are NOT supported.");
      return;
    }

    MessageBuilder messageBuilder = new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build());
    for (int i = 0; i < items.size(); i++) {
      Item item = items.get(i);
      messageBuilder.append((i > 0 ? "\n" : "") + item.getName() + " (" + item.getUrl() + ")");
    }
    // Add the attachments in reverse order so that they appear in the same order as the names.
    // Probably a Javacord bug.
    for (int i = items.size() - 1; i >= 0; i--) {
      Item item = items.get(i);
      Optional<File> maybeItemScreenshot = ItemScreenshotter.get(item);
      if (maybeItemScreenshot.isPresent()) {
        messageBuilder.addAttachment(maybeItemScreenshot.get());
      } else {
        messageBuilder.append("\n(Error fetching screenshot for item: " + item.getName() + ")");
      }
    }
    messageBuilder.send(event.getChannel());
  }
}
