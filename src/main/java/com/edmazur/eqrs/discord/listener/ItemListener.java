package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordServer;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.util.List;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ItemListener implements MessageCreateListener {

  private static final String TRIGGER = "!item";

  private final Config config;
  private final Discord discord;
  private final ItemDatabase itemDatabase;
  private final ItemScreenshotter itemScreenshotter;

  public ItemListener(
      Config config,
      Discord discord,
      ItemDatabase itemDatabase,
      ItemScreenshotter itemScreenshotter) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.itemDatabase = itemDatabase;
    this.itemScreenshotter = itemScreenshotter;
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (config.getBoolean(Property.DEBUG) && !DiscordServer.TEST.isEventServer(event)) {
      // In debug mode, ignore non-test server messages.
      return;
    } else if (!config.getBoolean(Property.DEBUG) && DiscordServer.TEST.isEventServer(event)) {
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
    List<Item> items = itemDatabase.parse(event.getMessageContent());
    if (items.isEmpty()) {
      event.getMessage().reply("Sorry, saw !item request, but couldn't match any item names. "
          + "Search is case-insensitive, but partials matches are NOT supported.");
      return;
    }

    // TODO: Factor out the code that's repeated here and in GratsListener.
    MessageBuilder messageBuilder = new MessageBuilder().replyTo(event.getMessage());
    for (int i = 0; i < items.size(); i++) {
      Item item = items.get(i);
      messageBuilder.append((i > 0 ? "\n" : "") + item.getName() + " (" + item.getUrl() + ")");
    }
    // Add the attachments in reverse order so that they appear in the same order as the names.
    // Probably a Javacord bug.
    for (int i = items.size() - 1; i >= 0; i--) {
      Item item = items.get(i);
      messageBuilder.addAttachment(itemScreenshotter.get(item));
    }
    messageBuilder.send(event.getChannel());
  }
}
