package com.edmazur.eqrs.game;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RaidTarget {

  private final String name;
  private final Optional<String> shortName;
  private final Set<String> aliases;
  private final List<Window> windows;

  public RaidTarget(
      String name,
      Optional<String> shortName,
      Set<String> aliases,
      List<Window> windows) {
    this.name = name;
    this.shortName = shortName;
    this.aliases = aliases;
    this.windows = windows;
  }

  public String getName() {
    return name;
  }

  public Optional<String> getShortName() {
    return shortName;
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
