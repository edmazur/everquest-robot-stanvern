package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  // These sets are a little hacky. These edge cases are quite rare considering how long the system
  // has been in place, but if the edge cases continue to grow, then you should think about a more
  // robust/graceful solution. Explanations:
  //
  // - IGNORED_TOKEN_EXCEPTIONS_CASE_INSENSITIVE
  //   There's a player named "Dkpp", which contains "dkp", which is an ignored token. There isn't
  //   really a good way to identify this sort of scenario because the ignored tokens are
  //   aggressively stripped out, given how they can often appear directly next to other valid text
  //   (e.g. "100dkp").
  //
  // - IGNORED_ITEMS
  //   There's a player named "Pebblespring", which contains "Espri", which is an item. There isn't
  //   really a good way to identify this sort of scenario because the item parser intentionally
  //   permits lack of whitespace before/after item names since they commonly get linked that way
  //   in-game.
  private static final Set<String> IGNORED_TOKEN_EXCEPTIONS_CASE_INSENSITIVE = Set.of("dkpp");
  private static final Set<String> IGNORED_ITEMS = Set.of("Espri");

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
    List<String> itemUrls = Lists.newArrayList();
    List<ValueOrError<File>> itemScreenshotsOrErrors = Lists.newArrayList();

    // Parse out the guild chat part.
    Matcher matcher = getPattern().matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return new GratsParseResult(
          eqLogEvent,
          itemUrls,
          ValueOrError.error("Error reading guild chat"),
          ValueOrError.error("Error reading guild chat"),
          itemScreenshotsOrErrors);
    }
    String gratsMessage = matcher.group(1);

    List<Item> items = itemDatabase.parse(gratsMessage);
    items.removeIf(item -> IGNORED_ITEMS.contains(item.getName()));
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
        getLootCommandOrError(gratsMessage, items),
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
  private ValueOrError<String> getLootCommandOrError(String gratsMessage, List<Item> items) {
    if (items.isEmpty()) {
      return ValueOrError.error("No items found");
    } else if (items.size() > 1) {
      return ValueOrError.error("Multiple items found");
    }
    Item item = items.get(0);

    // Remove the item name.
    Matcher matcher = item.getNamePattern().matcher(gratsMessage);
    if (!matcher.matches()) {
      return ValueOrError.error("Error getting item name");
    }
    gratsMessage = gratsMessage.replaceAll(matcher.group(1), "");

    List<Integer> numericOnlyParts = new ArrayList<Integer>();
    List<String> otherParts = new ArrayList<String>();
    for (String part : gratsMessage.trim().split("\\s+")) {
      // Remove ignored tokens.
      if (!IGNORED_TOKEN_EXCEPTIONS_CASE_INSENSITIVE.contains(part.toLowerCase())) {
        for (String ignoredTokenCaseInsensitive : IGNORED_TOKENS_CASE_INSENSITIVE) {
          part = part.replaceAll("(?i)" + ignoredTokenCaseInsensitive, "");
        }
      }
      if (part.isBlank()) {
        continue;
      }

      // Group what's remaining into categories.
      boolean partIsNonAlphaAndNumeric = part.matches("[^a-zA-Z]+") && part.matches(".*[0-9].*");
      if (partIsNonAlphaAndNumeric) {
        // Parts that are technically *not* numeric-only are still treated as numeric-only if their
        // non-numeric characters are all non-alpha. For example:
        // - "1337." will be treated as numeric-only because it's only non-numeric character is ".".
        // - "1337a" will *not* be treated as numeric-only because "a" is an alpha character.
        // The main goal is to permit stray non-alpha characters (e.g. "." and "/") in DKP values.
        String numericOnlyPart = part.replaceAll("[^0-9]", "");
        numericOnlyParts.add(Integer.parseInt(numericOnlyPart));
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
    if (!otherParts.get(0).matches("[a-zA-Z]+")) {
      return ValueOrError.error("Name candidate contains non-alpha characters: ``"
          + otherParts.get(0) + "``");
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
