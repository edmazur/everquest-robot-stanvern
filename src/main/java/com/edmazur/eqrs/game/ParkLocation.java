package com.edmazur.eqrs.game;

import java.util.List;

public class ParkLocation {

  private final int id;
  private final String name;
  private final List<String> aliases;

  public ParkLocation(int id, String name, List<String> aliases) {
    this.id = id;
    this.name = name;
    this.aliases = aliases;
  }

  public int getId() {
    return id;
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

}
