package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.discord.MessageBuilderFactory;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.channel.Channel;

public class GratsParser {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");
  private static final Pattern SAY_CHAT_PATTERN = Pattern.compile("You say, '(.+)'");

  private final Config config;
  private final ItemDatabase itemDatabase;
  private final EventChannelMatcher eventChannelMatcher;
  private final MessageBuilderFactory messageBuilderFactory;

  public GratsParser(
      Config config,
      ItemDatabase itemDatabase,
      EventChannelMatcher eventChannelMatcher,
      MessageBuilderFactory messageBuilderFactory) {
    this.config = config;
    this.itemDatabase = itemDatabase;
    this.eventChannelMatcher = eventChannelMatcher;
    this.messageBuilderFactory = messageBuilderFactory;
  }

  public GratsParseResult parse(EqLogEvent eqLogEvent) {
    List<Item> items = itemDatabase.parse(eqLogEvent.getPayload());
    return new GratsParseResult(
        eqLogEvent,
        items,
        getLootCommandOrError(eqLogEvent, items),
        getChannelMatchOrError(eqLogEvent, items),
        messageBuilderFactory);
  }

  /**
   * Tries to auto-parse the $loot string.
   * Example inputs:
   * - !grats Earthcaller stanvern 1000
   * - !grats Earthcaller 1000 STANVERN
   * - Earthcaller 1000 !grats Stanvern
   * - (and various other orderings, extra whitespace, etc.)
   * Expected output: $loot Earthcaller Stanvern 1000
   */
  private ValueOrError<String> getLootCommandOrError(EqLogEvent eqLogEvent, List<Item> items) {
    if (items.isEmpty()) {
      return ValueOrError.error("No items found");
    } else if (items.size() > 1) {
      return ValueOrError.error("Multiple items found");
    }
    Item item = items.get(0);

    Matcher matcher = getPattern().matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return ValueOrError.error("Error reading guild chat");
    }
    String gratsMessage = matcher.group(1).toLowerCase();

    // Remove the trigger.
    for (String trigger : GratsDetector.TRIGGERS) {
      gratsMessage = gratsMessage.replace(trigger, "");
    }

    // Remove the item name.
    gratsMessage = gratsMessage.replace(item.getName().toLowerCase(), "");

    // Group what's remaining into categories.
    List<String> alphaOnlyParts = new ArrayList<String>();
    List<Integer> numericOnlyParts = new ArrayList<Integer>();
    List<String> mixedParts = new ArrayList<String>();
    for (String part : gratsMessage.trim().split("\\s+")) {
      if (part.matches("[a-z]+")) {
        alphaOnlyParts.add(part);
      } else if (part.matches("[0-9]+")) {
        numericOnlyParts.add(Integer.parseInt(part));
      } else {
        mixedParts.add(part);
      }
    }

    // Validate mixed parts.
    // Do this first to give more helpful error messages (e.g. otherwise "0dkp" would trigger "No
    // DKP amount found").
    if (!mixedParts.isEmpty()) {
      return ValueOrError.error(
          "Unrecognized input found " + "(" + Joiner.on(", ").join(mixedParts) + ")");
    }

    // Validate alpha-only parts.
    if (alphaOnlyParts.isEmpty()) {
      return ValueOrError.error("No name found");
    } else if (alphaOnlyParts.size() > 1) {
      return ValueOrError.error(
          "Multiple name candidates found " + "(" + Joiner.on(", ").join(alphaOnlyParts) + ")");
    }

    // Validate numeric-only parts.
    if (numericOnlyParts.isEmpty()) {
      return ValueOrError.error("No DKP amount found");
    } else if (numericOnlyParts.size() > 1) {
      return ValueOrError.error("Multiple DKP amount candidates found "
          + "(" + Joiner.on(", ").join(numericOnlyParts) + ")");
    }

    // If you've gotten this far, there is a single name and number, so you can assume it's a player
    // name and DKP amount.
    String playerName = StringUtils.capitalize(alphaOnlyParts.get(0));
    int dkpAmount = numericOnlyParts.get(0);
    return ValueOrError.value("$loot " + item.getName() + " " + playerName + " " + dkpAmount);
  }

  private ValueOrError<Channel> getChannelMatchOrError(EqLogEvent eqLogEvent, List<Item> items) {
    if (items.isEmpty()) {
      return ValueOrError.error("No items found");
    } else if (items.size() > 1) {
      return ValueOrError.error("Multiple items found");
    }
    Item item = items.get(0);

    Optional<Channel> maybeChannel = eventChannelMatcher.getChannel(eqLogEvent, item);
    if (maybeChannel.isEmpty()) {
      return ValueOrError.error("Item not found in any event channel's loot table");
    }

    return ValueOrError.value(maybeChannel.get());
  }

  private Pattern getPattern() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return SAY_CHAT_PATTERN;
    } else {
      return GUILD_CHAT_PATTERN;
    }
  }

}
