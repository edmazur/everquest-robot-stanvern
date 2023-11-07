package com.edmazur.eqrs.game.listeners;

import com.edmazur.eqrs.game.RaidTarget;
import com.google.common.base.Preconditions;
import java.time.LocalDateTime;

public class GameTodParseResult {

  private final boolean parseSuccess;

  private final RaidTarget raidTarget;
  private final LocalDateTime timeOfDeath;

  private final String error;

  public static GameTodParseResult success(RaidTarget raidTarget, LocalDateTime timeOfDeath) {
    return new GameTodParseResult(true, raidTarget, timeOfDeath, null);
  }

  public static GameTodParseResult fail(String error) {
    return new GameTodParseResult(false, null, null, error);
  }

  private GameTodParseResult(
      boolean parseSuccess,
      RaidTarget raidTarget,
      LocalDateTime timeOfDeath,
      String error) {
    this.parseSuccess = parseSuccess;
    this.raidTarget = raidTarget;
    this.timeOfDeath = timeOfDeath;
    this.error = error;
  }

  public boolean wasSuccessfullyParsed() {
    return parseSuccess;
  }

  public RaidTarget getRaidTarget() {
    Preconditions.checkState(parseSuccess, "Method only valid for parse success");
    return raidTarget;
  }

  public LocalDateTime getTimeOfDeath() {
    Preconditions.checkState(parseSuccess, "Method only valid for parse success");
    return timeOfDeath;
  }

  public String getError() {
    Preconditions.checkState(!parseSuccess, "Method only valid for parse fail");
    return error;
  }

}
