package com.edmazur.eqrs.game;

import java.util.List;
import java.util.Set;

public class RaidTarget {

  private final String name;
  private final Set<String> aliases;
  private final List<Window> windows;

  public RaidTarget(
      String name,
      Set<String> aliases,
      List<Window> windows) {
    this.name = name;
    this.aliases = aliases;
    this.windows = windows;
  }

  public String getName() {
    return name;
  }

  public List<Window> getWindows() {
    return windows;
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
    return String.format("%s (aliases=%s, windows=%s)", name, aliases, windows);
  }

}
