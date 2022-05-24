package com.edmazur.eqrs.game;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.table.DataRow;
import com.edmazur.eqrs.table.HeaderRow;
import com.edmazur.eqrs.table.Justification;
import com.edmazur.eqrs.table.SubTable;
import com.edmazur.eqrs.table.Table;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class RaidTargetTableMaker {

  private static final String HUMAN_READABLE_TIMESTAMP_PATTERN = "EEE HH:mm:ss";

  private final RaidTargets raidTargets;
  private final DateTimeFormatter humanReadableTimestampFormatter;

  public RaidTargetTableMaker(Config config, RaidTargets raidTargets) {
    this.raidTargets = raidTargets;
    this.humanReadableTimestampFormatter =
        DateTimeFormatter.ofPattern(HUMAN_READABLE_TIMESTAMP_PATTERN)
            .withZone(ZoneId.of(config.getString(Property.TIMEZONE_GAME)));
  }

  public Table make() {
    // This is probably overkill, but cache "now" to avoid internal inconsistency in the event that
    // this function somehow takes more than a very small fraction of a second to complete.
    Instant now = Instant.now();

    Table table = new Table();
    SortedMap<Window.Status, SortedSet<RaidTarget>> raidTargetsByWindowStatus =
        getRaidTargetsByWindowStatus(now);
    for (Map.Entry<Window.Status, SortedSet<RaidTarget>> mapEntry :
        raidTargetsByWindowStatus.entrySet()) {
      Window.Status windowStatus = mapEntry.getKey();
      SortedSet<RaidTarget> raidTargets = mapEntry.getValue();

      if (windowStatus == Window.Status.PAST) {
        continue;
      }

      // Create each subtable.
      SubTable subTable = new SubTable();
      // TODO: Factor out the duplication here.
      switch (windowStatus) {
        case NOW:
          subTable.setHeading("Targets in window **NOW**:");
          subTable.setHeaderRow(new HeaderRow()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Left", Justification.RIGHT)
              .addColumn("Window Closes (ET)", Justification.LEFT)
              .addColumn("Window Closes (local)", Justification.LEFT));
          break;
        case SOON:
          subTable.setHeading(
              "Targets in window **SOON** (under " + Window.Status.SOON_DESCRIPTION + "):");
          subTable.setHeaderRow(new HeaderRow()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Left", Justification.RIGHT)
              .addColumn("Window Opens (ET)", Justification.LEFT)
              .addColumn("Window Opens (local)", Justification.LEFT));
          break;
        case LATER:
          subTable.setHeading(
              "Targets in window **LATER** (over " + Window.Status.SOON_DESCRIPTION + "):");
          subTable.setHeaderRow(new HeaderRow()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Left", Justification.RIGHT)
              .addColumn("Window Opens (ET)", Justification.LEFT)
              .addColumn("Window Opens (local)", Justification.LEFT));
          break;
        default:
          // Do nothing.
          break;
      }

      // Populate each subtable.
      for (RaidTarget raidTarget : raidTargets) {
        Window window = Window.getActiveWindow(raidTarget.getWindows(), now);
        Instant relevantWindowTimestamp = null;
        switch (windowStatus) {
          case NOW:
            relevantWindowTimestamp = window.getEnd();
            break;
          case SOON:
          case LATER:
            relevantWindowTimestamp = window.getStart();
            break;
          default:
            // Do nothing.
            break;
        }
        Duration timeLeft = Duration.between(now, relevantWindowTimestamp);

        DataRow dataRow = new DataRow();
        dataRow.addColumn(raidTarget.getName());
        dataRow.addColumn(formatHumanReadableDuration(timeLeft));
        dataRow.addColumn(formatHumanReadableTimestamp(relevantWindowTimestamp));
        dataRow.setCodeFontEndIndex(2);
        dataRow.addColumn(formatDiscordTimestamp(relevantWindowTimestamp));
        subTable.addDataRow(dataRow);
      }

      table.addSubTable(subTable);
    }
    return table;
  }

  private SortedMap<Window.Status, SortedSet<RaidTarget>> getRaidTargetsByWindowStatus(
      Instant now) {
    SortedMap<Window.Status, SortedSet<RaidTarget>> raidTargetsByWindowStatus = new TreeMap<>();
    RaidTargetComparator raidTargetComparator = new RaidTargetComparator(now);
    for (Window.Status windowStatus : Window.Status.values()) {
      raidTargetsByWindowStatus.put(windowStatus, new TreeSet<>(raidTargetComparator));
    }
    for (RaidTarget raidTarget : raidTargets.getAll()) {
      SortedSet<RaidTarget> raidTargets = raidTargetsByWindowStatus.get(
          Window.getActiveWindow(raidTarget.getWindows(), now).getStatus(now));
      raidTargets.add(raidTarget);
    }
    return raidTargetsByWindowStatus;
  }

  private String formatHumanReadableDuration(Duration duration) {
    List<String> timestampParts = new ArrayList<>();
    boolean hasDays = false;
    boolean hasHours = false;
    if (duration.toDays() > 0) {
      hasDays = true;
      timestampParts.add(String.format("%dd", duration.toDays()));
    }
    if (duration.toHoursPart() > 0 || hasDays) {
      hasHours = true;
      timestampParts.add(String.format("%2dh", duration.toHoursPart()));
    }
    if (duration.toMinutesPart() > 0 || hasDays || hasHours) {
      timestampParts.add(String.format("%2dm", duration.toMinutesPart()));
    }
    return String.join(" ", timestampParts);
  }

  private String formatHumanReadableTimestamp(Instant instant) {
    return humanReadableTimestampFormatter.format(instant);
  }

  private String formatDiscordTimestamp(Instant instant) {
    return String.format("<t:%d:F>", instant.getEpochSecond());
  }

}
