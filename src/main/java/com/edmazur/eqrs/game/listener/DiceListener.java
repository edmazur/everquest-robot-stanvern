package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.SoundPlayer;
import com.edmazur.eqrs.SoundPlayer.Sound;

public class DiceListener implements EqLogListener {

  private final DiceDetector diceDetector;
  private final SoundPlayer soundPlayer;

  public DiceListener(DiceDetector diceDetector, SoundPlayer soundPlayer) {
    this.diceDetector = diceDetector;
    this.soundPlayer = soundPlayer;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (diceDetector.containsDice(eqLogEvent)) {
      soundPlayer.play(Sound.DICE);
    }
  }

}
