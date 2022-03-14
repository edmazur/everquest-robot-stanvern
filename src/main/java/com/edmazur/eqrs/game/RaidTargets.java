package com.edmazur.eqrs.game;

import java.util.List;
import java.util.Optional;

import com.edmazur.eqrs.Database;

public class RaidTargets {

  private final List<RaidTarget> raidTargets;

  public RaidTargets(Database database) {
    this.raidTargets = database.getAllTargets();
  }

  public Optional<RaidTarget> getRaidTarget(String targetToParse) {
    for (RaidTarget raidTarget : raidTargets) {
      if (raidTarget.matchesName(targetToParse)) {
        return Optional.of(raidTarget);
      }
    }
    return Optional.empty();
  }

}