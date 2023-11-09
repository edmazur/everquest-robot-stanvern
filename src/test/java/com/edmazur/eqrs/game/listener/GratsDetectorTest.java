package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.edmazur.eqlp.EqLogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GratsDetectorTest {

  private GratsDetector gratsDetector;

  @BeforeEach
  void beforeEach() {
    gratsDetector = new GratsDetector();
  }

  @Test
  void gratssTrigger() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 17 01:20:19 2023] "
        + "Menehune tells the guild, 'Choker of the Wretched Menehune 400 Gratss'").get();
    assertTrue(gratsDetector.containsGrats(eqLogEvent));
  }

  @Test
  void exclamationGratsTrigger() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon Jun 05 18:55:11 2023] "
        + "Avitar tells the guild, '!grats Nature's Melody avitar 500'").get();
    assertTrue(gratsDetector.containsGrats(eqLogEvent));
  }

  @Test
  void exclamationGratzTrigger() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun Jun 04 21:35:28 2023] "
        + "Portola tells the guild, '!gratz Shield of Thorns Portola 133'").get();
    assertTrue(gratsDetector.containsGrats(eqLogEvent));
  }

  @Test
  void onlyTrigger() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon May 08 22:26:53 2023] Reptail tells the guild, 'gratsss'").get();
    assertFalse(gratsDetector.containsGrats(eqLogEvent));
  }

}
