package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.table.DataRow;
import com.edmazur.eqrs.table.Justification;
import com.edmazur.eqrs.table.SubTable;
import com.edmazur.eqrs.table.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DiscordTableFormatter {

  private static final int DEFAULT_SPACES_BETWEEN_COLUMNS = 4;
  // Discord has a message size limit of 2000 characters. Limit messages to a bit less than this.
  private static final int MESSAGE_SIZE_CAP = 1900;

  // These need to be referenced as their full names like this (as opposed to copy/pasted rendered
  // output) to avoid truncation issues when sending large messages.
  private static final String GREEN_SQUARE = ":green_square:";
  private static final String WHITE_SQUARE = ":white_large_square:";

  /**
   * Formats the table for Discord in a series of messages.
   *
   * <p>Returns one message per sub-table (with additional breaks within the table if needed) to
   * avoid hitting Discord message size limits.
   */
  public List<String> getMessages(
      Table table,
      Map<Integer, Integer> columnIndexToCustomRightSpacing) {
    List<String> messages = new ArrayList<>(table.getColumnCount());
    List<Integer> maxColumnWidths = table.getMaxColumnWidths();
    for (SubTable subTable : table.getSubTables()) {
      StringBuilder sb = new StringBuilder();
      sb.append(subTable.getHeading() + "\n");
      sb.append(getRow(
          subTable.getHeaderRow().getColumns(),
          table.getJustifications(),
          maxColumnWidths,
          Optional.empty(),
          columnIndexToCustomRightSpacing) + "\n");
      sb.append("`"
          + String.join(
              "",
              Collections.nCopies(getWidth(table, columnIndexToCustomRightSpacing), "-")) + "`\n");
      for (DataRow dataRow : subTable.getDataRows()) {
        String rowText = getRow(
            dataRow.getColumns(),
            table.getJustifications(),
            maxColumnWidths,
            dataRow.getCodeFontEndIndex(),
            columnIndexToCustomRightSpacing) + "\n";
        Optional<Double> maybeProgressPercentage = dataRow.getProgressPercentage();
        if (maybeProgressPercentage.isPresent()) {
          double progressPercentage = maybeProgressPercentage.get();
          int progressWidth = dataRow.getProgressWidth().get();
          rowText += getProgressBar(progressPercentage, progressWidth) + "\n";
        }

        // If this row would put us over the message size limit, split it off into a new message.
        if (sb.length() + rowText.length() > MESSAGE_SIZE_CAP) {
          messages.add(sb.toString());
          sb.setLength(0);
        }

        sb.append(rowText);
      }
      messages.add(sb.toString());
    }
    return messages;
  }

  private int getWidth(Table table, Map<Integer, Integer> columnIndexToCustomRightSpacing) {
    int width = 0;
    for (int maxColumnWidth : table.getMaxColumnWidths()) {
      width += maxColumnWidth;
    }
    for (int customRightSpacing : columnIndexToCustomRightSpacing.values()) {
      width += customRightSpacing;
    }
    width += DEFAULT_SPACES_BETWEEN_COLUMNS
        * (table.getColumnCount() - columnIndexToCustomRightSpacing.size() - 1);
    return width;
  }

  private String getRow(
      List<String> values,
      List<Justification> justifications,
      List<Integer> maxColumnWidths,
      Optional<Integer> codeFontEndIndex,
      Map<Integer, Integer> columnIndexToCustomRightSpacing) {
    List<String> columns = new ArrayList<>(values.size());
    for (int i = 0; i < values.size(); i++) {
      String value = values.get(i);
      Justification justification = justifications.get(i);
      String column = String.format(
          justification.getFormatString(maxColumnWidths.get(i)),
          value.replace("`", "'"));
      if (i == 0 || (codeFontEndIndex.isPresent() && i == codeFontEndIndex.get() + 1)) {
        column = "`" + column;
      }
      columns.add(column);
    }
    if (codeFontEndIndex.isEmpty()) {
      columns.set(columns.size() - 1, columns.get(columns.size() - 1) + "`");
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < columns.size(); i++) {
      sb.append(columns.get(i));
      // Add a column seperator to the right, but only if it's not the last column.
      if (i < columns.size() - 1) {
        int rightSpacing = columnIndexToCustomRightSpacing.containsKey(i)
            ? columnIndexToCustomRightSpacing.get(i)
            : DEFAULT_SPACES_BETWEEN_COLUMNS;
        sb.append(String.join("", Collections.nCopies(rightSpacing, " ")));
      }
    }
    return sb.toString();
  }

  private String getProgressBar(double percentage, int progressWidth) {
    StringBuilder sb = new StringBuilder();
    int greenSquares = (int) Math.round(progressWidth * percentage);
    int whiteSquares = progressWidth - greenSquares;
    sb.append(String.join("", Collections.nCopies(greenSquares, GREEN_SQUARE)));
    sb.append(String.join("", Collections.nCopies(whiteSquares, WHITE_SQUARE)));
    return sb.toString();
  }

}
