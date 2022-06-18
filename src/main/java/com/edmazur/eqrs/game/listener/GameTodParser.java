package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.game.RaidTarget;
import com.edmazur.eqrs.game.RaidTargets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class GameTodParser {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");

  // Heuristics based on offline historical data analysis.
  private static final Set<String> RELATIVE_TOD_INDICATORS =
      new HashSet<>(Arrays.asList("sec", "min", "hour"));
  private static final int MIN_LENGTH_TO_ALLOW_FUZZY_MATCH = 5;
  private static final int MAX_EDIT_DISTANCE = 2;

  private RaidTargets raidTargets;

  public GameTodParser(RaidTargets raidTargets) {
    this.raidTargets = raidTargets;
  }

  public Optional<GameTodParseResult> parse(EqLogEvent eqLogEvent) {
    Optional<String> maybeGuildChatText = getGuildChatMessage(eqLogEvent);
    if (maybeGuildChatText.isEmpty()) {
      return Optional.empty();
    }
    String guildChatText = maybeGuildChatText.get().toLowerCase();

    // Remove stuff that can get in the way of target detection: "tod" and extra whitespace.
    guildChatText = guildChatText.replace("tod", "").trim();

    // Give up if there's any indication that this is a relative ToD.
    boolean containsDigit = guildChatText.matches(".*\\d.*");
    boolean containsRelativeTodIndicator = false;
    for (String relativeTodIndicator : RELATIVE_TOD_INDICATORS) {
      if (guildChatText.contains(relativeTodIndicator)) {
        containsRelativeTodIndicator = true;
        break;
      }
    }
    if (containsDigit || containsRelativeTodIndicator) {
      return Optional.empty();
    }

    Optional<RaidTarget> maybeRaidTarget = getRaidTargetFuzzyMatch(guildChatText);
    if (maybeRaidTarget.isEmpty()) {
      return Optional.empty();
    }
    RaidTarget raidTarget = maybeRaidTarget.get();
    LocalDateTime timeOfDeath = eqLogEvent.getTimestamp();
    return Optional.of(new GameTodParseResult(raidTarget, timeOfDeath));
  }

  // TODO: Factor this out somewhere and re-use it in the Discord parser too.
  private Optional<RaidTarget> getRaidTargetFuzzyMatch(String text) {
    LevenshteinDistance editDistanceCalculator = new LevenshteinDistance(MAX_EDIT_DISTANCE);
    RaidTarget bestMatchRaidTarget = null;
    int bestMatchEditDistance = Integer.MAX_VALUE;
    List<String> parts = new ArrayList<>(Arrays.asList(text.split("\\s+")));
    // For each possible substring (broken up by whitespace)...
    for (int start = 0; start < parts.size(); start++) {
      for (int end = start + 1; end < parts.size() + 1; end++) {
        String subText = String.join(" ", parts.subList(start, end));
        // ...and for each possible target...
        for (RaidTarget raidTarget : raidTargets.getAllStaleAllowed()) {
          for (String nameForMatching : getNamesForMatching(raidTarget)) {
            boolean allowFuzzyMatch =
                nameForMatching.length() >= MIN_LENGTH_TO_ALLOW_FUZZY_MATCH;
            if (allowFuzzyMatch) {
              // If fuzzy match and it's better than what's been seen so far, save result.
              int editDistance = editDistanceCalculator.apply(nameForMatching, subText);
              if (editDistance != -1 && editDistance < bestMatchEditDistance) {
                bestMatchRaidTarget = raidTarget;
                bestMatchEditDistance = editDistance;
              }
            } else {
              // If exact match, return early.
              if (subText.equals(nameForMatching)) {
                return Optional.of(raidTarget);
              }
            }
          }
        }
      }
    }
    return Optional.ofNullable(bestMatchRaidTarget);
  }

  private static Set<String> getNamesForMatching(RaidTarget raidTarget) {
    Set<String> names = new HashSet<>();
    names.add(raidTarget.getName().toLowerCase());
    names.addAll(raidTarget.getAliases());
    // Empty alias names are in the data.
    // TODO: Fix this somewhere else further upstream.
    names.remove("");
    return names;
  }

  private Optional<String> getGuildChatMessage(EqLogEvent eqLogEvent) {
    Matcher matcher = GUILD_CHAT_PATTERN.matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.of(matcher.group(1));
  }

}
