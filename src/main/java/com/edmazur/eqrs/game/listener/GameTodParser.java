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
import org.apache.commons.text.similarity.LevenshteinDistance;

public class GameTodParser {

  private static final boolean DEBUG = false;

  private static final String TRIGGER = "!tod";

  // Heuristics based on offline historical data analysis.
  private static final Set<String> RELATIVE_TOD_INDICATORS =
      new HashSet<>(Arrays.asList("sec", "min", "hour"));
  private static final Set<String> NON_TOD_INDICATORS =
      new HashSet<>(Arrays.asList("skip", "unsure"));
  private static final int MIN_LENGTH_TO_ALLOW_FUZZY_MATCH = 5;
  private static final int MAX_EDIT_DISTANCE = 1;

  private RaidTargets raidTargets;

  public GameTodParser(RaidTargets raidTargets) {
    this.raidTargets = raidTargets;
  }

  public GameTodParseResult parse(EqLogEvent eqLogEvent, String todMessage) {
    if (!todMessage.toLowerCase().contains(TRIGGER)) {
      return GameTodParseResult.fail("Message did not contain \"" + TRIGGER + "\"");
    }

    // Remove stuff that can get in the way of target detection: uppercase, trigger, and extra
    // whitespace.
    todMessage = todMessage.toLowerCase().replace(TRIGGER, "").trim();

    // Give up if there's any indication that this is a non-ToD.
    if (todMessage.contains("?")) {
      return GameTodParseResult.fail("Saw a question mark, which means this might be a question "
          + "about a ToD");
    }
    for (String nonTodIndicator : NON_TOD_INDICATORS) {
      if (todMessage.contains(nonTodIndicator)) {
        return GameTodParseResult.fail("Saw `" + nonTodIndicator + "`, which means this might not "
            + "be a ToD");
      }
    }

    // Give up if there's any indication that this is a relative ToD.
    if (todMessage.matches(".*\\d.*")) {
      return GameTodParseResult.fail("Saw a number, which means this might be a relative ToD, "
          + "which I'm not smart enough to auto-parse");
    }
    for (String relativeTodIndicator : RELATIVE_TOD_INDICATORS) {
      if (todMessage.contains(relativeTodIndicator)) {
        return GameTodParseResult.fail(
            "Saw `" + relativeTodIndicator + "`, which means this might be a relative ToD, which "
                + "I'm not smart enough to auto-parse");
      }
    }

    Optional<RaidTarget> maybeRaidTarget = getRaidTargetFuzzyMatch(todMessage);
    if (maybeRaidTarget.isEmpty()) {
      return GameTodParseResult.fail("Did not find a known target name");
    }
    RaidTarget raidTarget = maybeRaidTarget.get();
    LocalDateTime timeOfDeath = eqLogEvent.getTimestamp();
    return GameTodParseResult.success(raidTarget, timeOfDeath);
  }

  // TODO: Factor this out somewhere and re-use it in the Discord parser too.
  private Optional<RaidTarget> getRaidTargetFuzzyMatch(String text) {
    LevenshteinDistance editDistanceCalculator = new LevenshteinDistance(MAX_EDIT_DISTANCE);
    RaidTarget bestFuzzyMatchRaidTarget = null;
    int bestFuzzyMatchEditDistance = Integer.MAX_VALUE;
    List<String> parts = new ArrayList<>(Arrays.asList(text.split("\\s+")));

    // For each possible substring (broken up by whitespace)...
    for (int start = 0; start < parts.size(); start++) {
      for (int end = start + 1; end < parts.size() + 1; end++) {
        String subText = String.join(" ", parts.subList(start, end));
        // ...and for each possible target...
        for (RaidTarget raidTarget : raidTargets.getAllStaleAllowed()) {
          for (String nameForMatching : getNamesForMatching(raidTarget)) {
            if (nameForMatching.length() >= MIN_LENGTH_TO_ALLOW_FUZZY_MATCH) {
              // If fuzzy match and it's better than what's been seen so far, save result.
              int editDistance = editDistanceCalculator.apply(nameForMatching, subText);
              if (editDistance != -1 && editDistance < bestFuzzyMatchEditDistance) {
                if (DEBUG) {
                  System.out.println("Found fuzzy match: " + subText);
                }
                bestFuzzyMatchRaidTarget = raidTarget;
                bestFuzzyMatchEditDistance = editDistance;
              }
            } else {
              // If exact match, return early.
              if (subText.equals(nameForMatching)) {
                if (DEBUG) {
                  System.out.println("Found exact match: " + subText);
                }
                return Optional.of(raidTarget);
              }
            }
          }
        }
      }
    }

    return Optional.ofNullable(bestFuzzyMatchRaidTarget);
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

}
