package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class ItemListener implements MessageCreateListener {

  private static final Pattern ITEM_PATTERN = Pattern.compile("!item.*");
  private static final Pattern ITEM_PARSE_PATTERN = Pattern.compile("!item (.+)");

  private final Discord discord;
  private final ItemDatabase itemDatabase;
  private final ItemScreenshotter itemScreenshotter;

  public ItemListener(
      Discord discord,
      ItemDatabase itemDatabase,
      ItemScreenshotter itemScreenshotter) {
    this.discord = discord;
    this.discord.addListener(this);
    this.itemDatabase = itemDatabase;
    this.itemScreenshotter = itemScreenshotter;
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    // Ignore yourself.
    if (event.getMessageAuthor().isYourself()) {
      return;
    }

    // TODO: Prevent replies on non-test server when in debug mode.

    Matcher itemMatcher = ITEM_PATTERN.matcher(event.getMessageContent());
    if (itemMatcher.matches()) {
      Matcher itemParseMatcher = ITEM_PARSE_PATTERN.matcher(event.getMessageContent());
      if (!itemParseMatcher.matches()) {
        event.getMessage().reply("Sorry, couldn't parse !item command.");
        return;
      }

      String itemToSearchFor = itemParseMatcher.group(1);
      Optional<Item> maybeItem = itemDatabase.getByName(itemToSearchFor);
      if (maybeItem.isEmpty()) {
        event.getMessage().reply("Sorry, couldn't find item. Note that only case-sensitive exact "
            + "match is currently supported.");
        return;
      }

      event.getChannel().type();
      Item item = maybeItem.get();
      File screenshot = itemScreenshotter.get(item);
      new MessageBuilder()
          .append(item.getName() + " (" + item.getUrl() + ")")
          .addAttachment(screenshot)
          .replyTo(event.getMessage())
          .send(event.getChannel());
    }
  }
}
