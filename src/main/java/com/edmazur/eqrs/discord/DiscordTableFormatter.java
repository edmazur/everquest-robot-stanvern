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

  /**
   * Formats the table for Discord.
   *
   * <p>Returns one block of text per sub-table to avoid hitting Discord message size limits. This
   * could probably be more handled more intelligently.
   */
  public List<String> format(Table table) {
    List<String> formattedTable = new ArrayList<>(table.getColumnCount());
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
        sb.append(getRow(
            dataRow.getColumns(),
            table.getJustifications(),
            maxColumnWidths,
            dataRow.getCodeFontEndIndex()) + "\n");
      }
      formattedTable.add(sb.toString());
    }
    return formattedTable;
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
