package com.edmazur.eqrs.game.listener;

import com.beust.jcommander.internal.Lists;
import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import com.google.common.base.Joiner;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public class GratsParser {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");
  private static final Pattern SAY_CHAT_PATTERN = Pattern.compile("You say, '(.+)'");
  private static final List<String> IGNORED_TOKENS_CASE_INSENSITIVE =
      Stream.concat(
          List.of(
              "closed",
              "dkp",
              "swapping").stream(),
          GratsDetector.TRIGGERS.stream())
      .collect(Collectors.toList());
  private static final String LOOT_COMMAND_FORMAT = "$loot %s %s %d";

  private final Config config;
  private final ItemDatabase itemDatabase;
  private final ItemScreenshotter itemScreenshotter;
  private final EventChannelMatcher eventChannelMatcher;

  public GratsParser(
      Config config,
      ItemDatabase itemDatabase,
      ItemScreenshotter itemScreenshotter,
      EventChannelMatcher eventChannelMatcher) {
    this.config = config;
    this.itemDatabase = itemDatabase;
    this.itemScreenshotter = itemScreenshotter;
    this.eventChannelMatcher = eventChannelMatcher;
  }

  public GratsParseResult parse(EqLogEvent eqLogEvent) {
    List<Item> items = itemDatabase.parse(eqLogEvent.getPayload());
    List<String> itemUrls = Lists.newArrayList();
    List<ValueOrError<File>> itemScreenshotsOrErrors = Lists.newArrayList();
    for (Item item : items) {
      itemUrls.add(item.getUrl());
      Optional<File> maybeItemScreenshot = itemScreenshotter.get(item);
      if (maybeItemScreenshot.isPresent()) {
        itemScreenshotsOrErrors.add(ValueOrError.value(maybeItemScreenshot.get()));
      } else {
        itemScreenshotsOrErrors.add(
            ValueOrError.error("(Error fetching screenshot for item: ``" + item.getName() + "``)"));
      }
    }
    return new GratsParseResult(
        eqLogEvent,
        itemUrls,
        getLootCommandOrError(eqLogEvent, items),
        eventChannelMatcher.getChannel(eqLogEvent, items),
        itemScreenshotsOrErrors);
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
    String gratsMessage = matcher.group(1);

    // Remove the item name.
    matcher = item.getNamePattern().matcher(gratsMessage);
    if (!matcher.matches()) {
      return ValueOrError.error("Error getting item name");
    }
    gratsMessage = gratsMessage.replaceAll(matcher.group(1), "");

    List<Integer> numericOnlyParts = new ArrayList<Integer>();
    List<String> otherParts = new ArrayList<String>();
    for (String part : gratsMessage.trim().split("\\s+")) {
      // Remove ignored tokens.
      for (String ignoredTokenCaseInsensitive : IGNORED_TOKENS_CASE_INSENSITIVE) {
        part = part.replaceAll("(?i)" + ignoredTokenCaseInsensitive, "");
      }
      if (part.isBlank()) {
        continue;
      }

      // Group what's remaining into categories.
      if (part.matches("[0-9]+")) {
        numericOnlyParts.add(Integer.parseInt(part));
      } else {
        otherParts.add(part);
      }
    }

    // Remove parts that consist of only non-alphanumeric characters (e.g. "," and "!!").
    otherParts.removeIf(part -> part.matches("[^a-zA-Z0-9]+"));

    // Validate numeric-only parts.
    if (numericOnlyParts.isEmpty()) {
      return ValueOrError.error("No DKP amount found");
    } else if (numericOnlyParts.size() > 1) {
      return ValueOrError.error("Multiple DKP amount candidates found: ``"
          + Joiner.on("``, ``").join(numericOnlyParts) + "``");
    }
    int dkpAmount = numericOnlyParts.get(0);

    // Validate other parts.
    if (otherParts.size() != 1) {
      return ValueOrError.error(
          "``"
          + String.format(LOOT_COMMAND_FORMAT,
              item.getNameWithBackticksReplaced(), "???", dkpAmount)
          + "`` "
          + (otherParts.isEmpty()
              ? "(No name found)"
              : "(Multiple name candidates found: ``"
                  + Joiner.on("``, ``").join(otherParts) + "``)"));
    }
    String playerName = StringUtils.capitalize(otherParts.get(0).toLowerCase());

    // If you've gotten this far, there is a single name and number, so you can assume it's a player
    // name and DKP amount.
    return ValueOrError.value(String.format(LOOT_COMMAND_FORMAT,
        item.getNameWithBackticksReplaced(), playerName, dkpAmount));
  }

  private Pattern getPattern() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return SAY_CHAT_PATTERN;
    } else {
      return GUILD_CHAT_PATTERN;
    }
  }

}
