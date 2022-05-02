package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiceDetector {

  private static final Pattern DICE_PATTERN = Pattern.compile(
      "\\*\\*It could have been any number from \\d+ to \\d+, but this time it turned up a \\d+.");

  public boolean containsDice(EqLogEvent eqLogEvent) {
    Matcher matcher = DICE_PATTERN.matcher(eqLogEvent.getPayload());
    return matcher.matches();
  }

}
