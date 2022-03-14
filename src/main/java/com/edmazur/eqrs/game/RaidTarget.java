package com.edmazur.eqrs.game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RaidTarget {

  private final String name;
  private final Set<String> aliases;

  public RaidTarget(String name, String... aliases) {
    this.name = name;
    this.aliases = new HashSet<String>(Arrays.asList(aliases));
  }

  public String getName() {
    return name;
  }

  public boolean matchesName(String nameToCheck) {
    if (name.equalsIgnoreCase(nameToCheck)) {
      return true;
    }
    for (String alias : aliases) {
      if (alias.equalsIgnoreCase(nameToCheck)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return name + " (" + aliases + ")";
  }

}