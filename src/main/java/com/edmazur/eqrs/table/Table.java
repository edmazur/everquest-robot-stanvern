package com.edmazur.eqrs.table;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single table broken up into multiple sub-tables.
 *
 * <p>All sub-tables are assumed to have the same number of columns and same column justifications.
 */
public class Table {

  private List<SubTable> subTables = new ArrayList<>();

  public Table addSubTable(SubTable subTable) {
    this.subTables.add(subTable);
    return this;
  }

  public List<SubTable> getSubTables() {
    return subTables;
  }

  public List<Integer> getMaxColumnWidths() {
    List<Integer> maxColumnWidths = new ArrayList<>(getColumnCount());
    for (int i = 0; i < getColumnCount(); i++) {
      maxColumnWidths.add(getMaxColumnWidth(i));
    }
    return maxColumnWidths;
  }

  public int getColumnCount() {
    return subTables.get(0).getColumnCount();
  }

  public List<Justification> getJustifications() {
    return subTables.get(0).getJustifications();
  }

  private int getMaxColumnWidth(int i) {
    int tableMaxColumnWidth = 0;
    for (SubTable subTable : subTables) {
      int subTableMaxColumnWidth = subTable.getMaxColumnWidth(i);
      tableMaxColumnWidth = Math.max(tableMaxColumnWidth, subTableMaxColumnWidth);
    }
    return tableMaxColumnWidth;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (SubTable subTable : subTables) {
      sb.append(subTable + "\n");
    }
    return sb.toString();
  }

}
