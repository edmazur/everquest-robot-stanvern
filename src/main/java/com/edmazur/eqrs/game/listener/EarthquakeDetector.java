package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EarthquakeDetector {

  private static final List<String> KNOWN_EARTHQUAKE_STRINGS = Arrays.asList(
      "The Gods of Norrath emit a sinister laugh as they toy with their creations. "
          + "They are reanimating creatures to provide a greater challenge to the mortals.",
      "The Gods of Norrath emit a sinister laugh as they toy with their creations. "
          + "They are reanimating creatures to provide a greater challenge to the mortals",
      "The gods have awoken to unleash their wrath across Norrath.");

  private static final List<Pattern> KNOWN_EARTHQUAKE_PATTERNS = Arrays.asList(
      Pattern.compile("\\p{Alpha}+ BROADCASTS, "
          + "'Minions gather, their forms appearing as time and space coalesce.'"));

  public boolean containsEarthquake(EqLogEvent eqLogEvent) {
    for (String knownEarthquakeString : KNOWN_EARTHQUAKE_STRINGS) {
      if (eqLogEvent.getPayload().equals(knownEarthquakeString)) {
        return true;
      }
    }

    for (Pattern knownEarthquakePattern : KNOWN_EARTHQUAKE_PATTERNS) {
      Matcher matcher = knownEarthquakePattern.matcher(eqLogEvent.getPayload());
      if (matcher.matches()) {
        return true;
      }
    }

    return false;
  }

}
