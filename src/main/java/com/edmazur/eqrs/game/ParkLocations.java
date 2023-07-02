package com.edmazur.eqrs.game;

import java.util.List;
import java.util.Optional;

public class ParkLocations {

  private final List<ParkLocation> parkLocations;

  public ParkLocations(List<ParkLocation> parkLocations) {
    this.parkLocations = parkLocations;
  }

  public Optional<ParkLocation> getParkLocation(String input) {
    for (ParkLocation parkLocation : parkLocations) {
      if (parkLocation.matchesName(input)) {
        return Optional.of(parkLocation);
      }
    }
    return Optional.empty();
  }

}
