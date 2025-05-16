package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import java.time.Duration;
import java.time.LocalDateTime;
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
      "The gods have awoken to unleash their wrath across Norrath.",
      "The gods have awoken to unlesh their wrath across Norrath.",
      "An unsettling silence smothers the land. Not a complete silence, but somehow quieter for "
          + "it, the way a thick blanket of snow muffles the noise of the world. The chill of it "
          + "pierces your bones, and you know, danger approaches.");

  private static final List<Pattern> KNOWN_EARTHQUAKE_PATTERNS = Arrays.asList(
      Pattern.compile("\\p{Alpha}+ BROADCASTS, "
          + "'Minions gather, their forms appearing as time and space coalesce.'"));

  // Sometimes new earthquake messages are added. This failsafe string often (not always) appears
  // after earthquake messages, so it can be used as an additional trigger if one of the known
  // strings hasn't recently triggered.
  // Unfortunately, this is not a 100% reliable earthquake indicator (i.e. earthquakes sometimes
  // happen without it), so the known strings list is still needed.
  private static final String FAILSAFE_STRING = "You feel the need to get somewhere safe quickly.";
  private static final Duration FAILSAFE_WINDOW = Duration.ofMinutes(1);

  private LocalDateTime lastSeenEarthquake = LocalDateTime.MIN;

  public boolean containsEarthquake(EqLogEvent eqLogEvent) {
    for (String knownEarthquakeString : KNOWN_EARTHQUAKE_STRINGS) {
      if (eqLogEvent.getPayload().equals(knownEarthquakeString)) {
        lastSeenEarthquake = eqLogEvent.getTimestamp();
        return true;
      }
    }

    for (Pattern knownEarthquakePattern : KNOWN_EARTHQUAKE_PATTERNS) {
      Matcher matcher = knownEarthquakePattern.matcher(eqLogEvent.getPayload());
      if (matcher.matches()) {
        lastSeenEarthquake = eqLogEvent.getTimestamp();
        return true;
      }
    }

    if (eqLogEvent.getPayload().equals(FAILSAFE_STRING)
        && lastSeenEarthquake.plus(FAILSAFE_WINDOW).isBefore(eqLogEvent.getTimestamp())) {
      lastSeenEarthquake = eqLogEvent.getTimestamp();
      return true;
    }

    return false;
  }

}
