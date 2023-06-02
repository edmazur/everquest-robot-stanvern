package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.discord.MessageBuilderFactory;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.message.MessageBuilder;

public class GratsParseResult {

  private static final char SUCCESS_ICON = '✅';
  private static final char ERROR_ICON = '❌';
  private static final String LOOT_COMMAND_LABEL = "$loot command";
  private static final String CHANNEL_MATCH_LABEL = "Channel match";

  private final EqLogEvent eqLogEvent;
  private final List<Item> items;
  private final ValueOrError<String> lootCommandOrError;
  private final ValueOrError<Channel> channelMatchOrError;
  private final MessageBuilderFactory messageBuilderFactory;

  public GratsParseResult(
      EqLogEvent eqLogEvent,
      List<Item> items,
      ValueOrError<String> lootCommandOrError,
      ValueOrError<Channel> channelMatchOrError,
      MessageBuilderFactory messageBuilderFactory) {
    this.eqLogEvent = eqLogEvent;
    this.items = items;
    this.lootCommandOrError = lootCommandOrError;
    this.channelMatchOrError = channelMatchOrError;
    this.messageBuilderFactory = messageBuilderFactory;
  }

  public MessageBuilder getMessageBuilder(ItemScreenshotter itemScreenshotter) {
    MessageBuilder messageBuilder = messageBuilderFactory.create();

    // Add raw !grats message.
    messageBuilder
        .append("💰 ET: `" + eqLogEvent.getFullLine() + "`")
        .appendNewLine();

    // Add loot command.
    if (lootCommandOrError.hasError()) {
      messageBuilder
          .append(ERROR_ICON + " **" + LOOT_COMMAND_LABEL
              + "**: " + lootCommandOrError.getError())
          .appendNewLine();
    } else {
      messageBuilder
          .append(SUCCESS_ICON + " **" + LOOT_COMMAND_LABEL
              + "**: `" + lootCommandOrError.getValue() + "`")
          .appendNewLine();
    }

    // Add channel match.
    if (channelMatchOrError.hasError()) {
      messageBuilder
          .append(ERROR_ICON + " **" + CHANNEL_MATCH_LABEL
              + "**: " + channelMatchOrError.getError())
          .appendNewLine();
    } else {
      messageBuilder
          .append(SUCCESS_ICON + " **" + CHANNEL_MATCH_LABEL
              + "**: <#" + channelMatchOrError.getValue().getId() + ">")
          .appendNewLine();
    }

    // Add item links.
    for (Item item : items) {
      messageBuilder
          .append(item.getUrl())
          .appendNewLine();
    }

    // Add item screenshots.
    // Do this in reverse order so that they appear in the same order as the names.
    // Probably a Javacord bug.
    for (int i = items.size() - 1; i >= 0; i--) {
      Item item = items.get(i);
      Optional<File> maybeItemScreenshot = itemScreenshotter.get(item);
      if (maybeItemScreenshot.isPresent()) {
        messageBuilder.addAttachment(maybeItemScreenshot.get());
      } else {
        messageBuilder
            .append("(Error fetching screenshot for item: " + item.getName() + ")")
            .appendNewLine();
      }
    }

    return messageBuilder;
  }

}
