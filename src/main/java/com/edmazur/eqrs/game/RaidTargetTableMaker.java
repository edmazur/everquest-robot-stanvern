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

  // Including Discord timestamps makes the table width too wide to render well on mobile devices,
  // so they're disabled by default. The code is left here in case it's ever needed, e.g. if a
  // separate non-mobile channel is created.
  private static final boolean SHOW_DISCORD_TIMESTAMPS = false;

  private static final String HUMAN_READABLE_TIMESTAMP_PATTERN = "EEE HH:mm:ss";

  // TODO: Remove the ability to set different progress bar widths if you keep this as a constant.
  private static final int MIN_PROGRESS_BAR_WIDTH = 16;
  private static final int MAX_PROGRESS_BAR_WIDTH = 16;

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
      // This is final to avoid a VariableDeclarationUsageDistance checkstyle warning.
      // TODO: Disable that check?
      final SortedSet<RaidTarget> raidTargets = mapEntry.getValue();

      if (windowStatus == Window.Status.PAST) {
        continue;
      }

      // Create each subtable.
      SubTable subTable = new SubTable();
      HeaderRow headerRow = new HeaderRow();
      // TODO: Factor out the duplication here.
      switch (windowStatus) {
        case NOW:
          subTable.setHeading("Targets in window **NOW**:");
          headerRow
              .addEmptyColumn()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Left", Justification.RIGHT)
              .addColumn("Closes (ET)", Justification.RIGHT);
          if (SHOW_DISCORD_TIMESTAMPS) {
            headerRow.addColumn("Closes (local)", Justification.LEFT);
          }
          break;
        case SOON:
          subTable.setHeading(
              "Targets in window **SOON** (under " + Window.Status.SOON_DESCRIPTION + "):");
          headerRow
              .addEmptyColumn()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Until", Justification.RIGHT)
              .addColumn("Opens (ET)", Justification.RIGHT);
          if (SHOW_DISCORD_TIMESTAMPS) {
            headerRow.addColumn("Opens (local)", Justification.LEFT);
          }
          break;
        case LATER:
          subTable.setHeading(
              "Targets in window **LATER** (over " + Window.Status.SOON_DESCRIPTION + "):");
          headerRow
              .addEmptyColumn()
              .addColumn("Name", Justification.LEFT)
              .addColumn("Time Until", Justification.RIGHT)
              .addColumn("Opens (ET)", Justification.RIGHT);
          if (SHOW_DISCORD_TIMESTAMPS) {
            headerRow.addColumn("Opens (local)", Justification.LEFT);
          }
          break;
        default:
          // Do nothing.
          break;
      }
      subTable.setHeaderRow(headerRow);

      // Populate each subtable.
      for (RaidTarget raidTarget : raidTargets) {
        Window window = Window.getActiveWindow(raidTarget.getWindows(), now);
        Instant relevantWindowTimestamp = null;
        Double progressPercentage = null;
        Integer progressWidth = null;
        switch (windowStatus) {
          case NOW:
            relevantWindowTimestamp = window.getEnd();
            progressPercentage = window.getPercentPassed(now);
            progressWidth = Math.min(MAX_PROGRESS_BAR_WIDTH,
                Math.max(MIN_PROGRESS_BAR_WIDTH, (int) window.getDuration().toHours()));
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
        dataRow.addColumn(window.getExtrapolationCount() == 0
            ? "" : "[" + window.getExtrapolationCount() + "]");
        dataRow.addColumn(raidTarget.getShortName().isPresent()
            ? raidTarget.getShortName().get() : raidTarget.getName());
        dataRow.addColumn(formatHumanReadableDuration(timeLeft));
        dataRow.addColumn(formatHumanReadableTimestamp(relevantWindowTimestamp));
        if (SHOW_DISCORD_TIMESTAMPS) {
          dataRow.setCodeFontEndIndex(2);
          dataRow.addColumn(formatDiscordTimestamp(relevantWindowTimestamp));
        }
        if (progressPercentage != null) {
          dataRow.setProgress(progressPercentage, progressWidth);
        }
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
