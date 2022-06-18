package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.game.RaidTarget;
import java.time.LocalDateTime;

public class GameTodParseResult {

  private RaidTarget raidTarget;
  private LocalDateTime timeOfDeath;

  public GameTodParseResult(RaidTarget raidTarget, LocalDateTime timeOfDeath) {
    this.raidTarget = raidTarget;
    this.timeOfDeath = timeOfDeath;
  }

  public RaidTarget getRaidTarget() {
    return raidTarget;
  }

  public LocalDateTime getTimeOfDeath() {
    return timeOfDeath;
  }

}
