package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.table.DataRow;
import com.edmazur.eqrs.table.Justification;
import com.edmazur.eqrs.table.SubTable;
import com.edmazur.eqrs.table.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DiscordTableFormatter {

  private static final int SPACES_BETWEEN_COLUMNS = 4;
  private static final String COLUMN_SEPARATOR =
      String.join("", Collections.nCopies(SPACES_BETWEEN_COLUMNS, " "));
  // Discord has a message size limit of 2000 characters. Limit messages to a bit less than this.
  private static final int MESSAGE_SIZE_CAP = 1900;

  /**
   * Formats the table for Discord in a series of messages.
   *
   * <p>Returns one message per sub-table (with additional breaks within the table if needed) to
   * avoid hitting Discord message size limits.
   */
  public List<String> getMessages(Table table) {
    List<String> messages = new ArrayList<>(table.getColumnCount());
    List<Integer> maxColumnWidths = table.getMaxColumnWidths();
    for (SubTable subTable : table.getSubTables()) {
      StringBuilder sb = new StringBuilder();
      sb.append(subTable.getHeading() + "\n");
      sb.append(getRow(
          subTable.getHeaderRow().getColumns(),
          table.getJustifications(),
          maxColumnWidths,
          Optional.empty()) + "\n");
      sb.append("`" + String.join("", Collections.nCopies(getWidth(table), "-")) + "`\n");
      for (DataRow dataRow : subTable.getDataRows()) {
        String rowText = getRow(
            dataRow.getColumns(),
            table.getJustifications(),
            maxColumnWidths,
            dataRow.getCodeFontEndIndex()) + "\n";

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

  private int getWidth(Table table) {
    int width = 0;
    for (int maxColumnWidth : table.getMaxColumnWidths()) {
      width += maxColumnWidth;
    }
    width += SPACES_BETWEEN_COLUMNS * (table.getColumnCount() - 1);
    return width;
  }

  private String getRow(
      List<String> values,
      List<Justification> justifications,
      List<Integer> maxColumnWidths,
      Optional<Integer> codeFontEndIndex) {
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
    return String.join(COLUMN_SEPARATOR, columns);
  }

}
