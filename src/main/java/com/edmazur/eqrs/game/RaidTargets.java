package com.edmazur.eqrs.game;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Json;
import com.edmazur.eqrs.Logger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class RaidTargets {

  private static final Logger LOGGER = new Logger();

  private final Config config;
  private final Json json;

  private List<RaidTarget> raidTargets;

  public RaidTargets(Config config, Json json) {
    this.config = config;
    this.json = json;
  }

  /**
   * Gets all raid targets.
   *
   * <p>Always fresh read.
   */
  public List<RaidTarget> getAll() {
    updateCache();
    return getAllInternal();
  }

  /**
   * Gets all raid targets.
   *
   * <p>Almost always stale read (exception: first call, if updateCache() not called before).
   */
  public List<RaidTarget> getAllStaleAllowed() {
    return getAllInternal();
  }

  /**
   * Gets the raid target matching the name. Search is case-insensitive and will match on aliases.
   *
   * <p>Almost always stale read (exception: first call, if updateCache() not called before).
   */
  public Optional<RaidTarget> getRaidTarget(String targetToParse) {
    for (RaidTarget raidTarget : getAllInternal()) {
      if (raidTarget.matchesName(targetToParse)) {
        return Optional.of(raidTarget);
      }
    }
    return Optional.empty();
  }

  /**
   * Internal callers must always call this to access raidTargets.
   *
   * <p>Almost always stale read (exception: first call, if updateCache() not called before).
   */
  private List<RaidTarget> getAllInternal() {
    if (raidTargets == null) {
      updateCache();
    }
    return raidTargets;
  }

  private void updateCache() {
    Optional<JSONObject> maybeJsonObject =
        json.read(config.getString(Config.Property.EVERQUEST_RAID_TARGETS_ENDPOINT));
    if (maybeJsonObject.isEmpty()) {
      // TODO: Do something more intelligent here.
      LOGGER.log("Unable to update cache");
      return;
    }

    JSONArray raidTargetsJson = maybeJsonObject.get().getJSONArray("raidTargets");
    List<RaidTarget> raidTargets = new ArrayList<>(raidTargetsJson.length());
    for (int i = 0; i < raidTargetsJson.length(); i++) {
      JSONObject raidTargetJson = raidTargetsJson.getJSONObject(i);
      String name = raidTargetJson.getString("name");
      String shortName = raidTargetJson.getString("shortName");
      Set<String> aliases = Set.of(raidTargetJson.getString("aliases").split(","));
      JSONArray windowsJson = raidTargetJson.getJSONArray("windows");
      List<Window> windows = new ArrayList<>(windowsJson.length());
      for (int j = 0; j < windowsJson.length(); j++) {
        JSONObject windowJson = windowsJson.getJSONObject(j);
        Instant start = Instant.ofEpochSecond(windowJson.getLong("start"));
        Instant end = Instant.ofEpochSecond(windowJson.getLong("end"));
        int extrapolationCount = windowJson.getInt("extrapolationCount");
        Window window = new Window(start, end, extrapolationCount);
        windows.add(window);
      }
      RaidTarget raidTarget = new RaidTarget(
          name,
          shortName.isEmpty() ? Optional.empty() : Optional.of(shortName),
          aliases,
          windows);
      raidTargets.add(raidTarget);
    }
    this.raidTargets = raidTargets;
  }

}
