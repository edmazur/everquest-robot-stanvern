package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.edmazur.eqlp.EqLogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EarthquakeDetectorTest {

  private EarthquakeDetector earthquakeDetector;

  @BeforeEach
  void beforeEach() {
    earthquakeDetector = new EarthquakeDetector();
  }

  @Test
  void sinisterLaugh() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Mar 26 12:21:43 2021] The Gods of Norrath emit a sinister laugh as they toy with "
            + "their creations. They are reanimating creatures to provide a greater challenge to "
            + "the mortals.").get();
    assertTrue(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void sinisterLaughWithoutPeriod() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Mar 26 12:21:43 2021] The Gods of Norrath emit a sinister laugh as they toy with "
            + "their creations. They are reanimating creatures to provide a greater challenge to "
            + "the mortals").get();
    assertTrue(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void godsAwoken() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:17:32 2022] The gods have awoken to unleash their wrath across Norrath.")
            .get();
    assertTrue(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void godsAwokenUnlesh() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sat Jun 24 07:16:38 2023] The gods have awoken to unlesh their wrath across Norrath.")
            .get();
    assertTrue(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void nilbogBroadcast() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun Oct 17 00:10:33 2021] Nilbog BROADCASTS, 'Minions gather, their forms appearing as "
            + "time and space coalesce.'").get();
    assertTrue(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void copyPasteFullLine() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Feb 10 23:48:13 2022] Alou tells the guild, '[Sat Feb 05 21:44:16 2022] The Gods of "
            + "Norrath emit a sinister laugh as they toy with their creations. They are "
            + "reanimating creatures to provide a greater challenge to the mortals'").get();
    assertFalse(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void copyPastePartialLine() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sat Mar 19 01:12:55 2022] Avitar tells the guild, 'The Gods of Norrath emit a sinister "
            + "laugh as they toy with their creations.'").get();
    assertFalse(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void trollMotd() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Mar 25 18:48:26 2022] GUILD MOTD: Fray - The Gods of Norrath emit a sinister laugh "
            + "as they toy with their creations. They are reanimating creatures to provide a "
            + "greater challenge to the mortals").get();
    assertFalse(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void trollEmote() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sat Feb 13 00:06:52 2021] Untwinked Minions gather, their forms appearing as time and "
            + "space coalesce... You feel as though you should get to safety.").get();
    assertFalse(earthquakeDetector.containsEarthquake(eqLogEvent));
  }

  @Test
  void failsafeNotNeededWithinWindow() {
    EqLogEvent knownEarthquakeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:17:32 2022] The gods have awoken to unleash their wrath across Norrath.")
            .get();
    EqLogEvent failsafeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:18:31 2022] You feel the need to get somewhere safe quickly.")
            .get();
    assertTrue(earthquakeDetector.containsEarthquake(knownEarthquakeEvent));
    assertFalse(earthquakeDetector.containsEarthquake(failsafeEvent));
  }

  @Test
  void failsafeNotNeededOutsideWindow() {
    EqLogEvent knownEarthquakeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:17:32 2022] The gods have awoken to unleash their wrath across Norrath.")
            .get();
    EqLogEvent failsafeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:18:33 2022] You feel the need to get somewhere safe quickly.")
            .get();
    assertTrue(earthquakeDetector.containsEarthquake(knownEarthquakeEvent));
    assertTrue(earthquakeDetector.containsEarthquake(failsafeEvent));
  }

  @Test
  void failsafeNeededWithinWindow() {
    EqLogEvent unknownEarthquakeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:17:32 2022] Brand new earthquake message.").get();
    EqLogEvent failsafeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:18:31 2022] You feel the need to get somewhere safe quickly.")
            .get();
    assertFalse(earthquakeDetector.containsEarthquake(unknownEarthquakeEvent));
    assertTrue(earthquakeDetector.containsEarthquake(failsafeEvent));
  }

  @Test
  void failsafeNeededOutsideWindow() {
    EqLogEvent unknownEarthquakeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:17:32 2022] Brand new earthquake message.").get();
    EqLogEvent failsafeEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 29 13:18:33 2022] You feel the need to get somewhere safe quickly.")
            .get();
    assertFalse(earthquakeDetector.containsEarthquake(unknownEarthquakeEvent));
    assertTrue(earthquakeDetector.containsEarthquake(failsafeEvent));
  }

}
