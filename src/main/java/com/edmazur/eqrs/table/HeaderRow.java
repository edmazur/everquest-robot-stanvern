package com.edmazur.eqrs.table;

import java.util.ArrayList;
import java.util.List;

public class HeaderRow {

  private List<String> columns = new ArrayList<>();
  private List<Justification> justifications = new ArrayList<>();

  public HeaderRow addColumn(String column, Justification justification) {
    this.columns.add(column);
    this.justifications.add(justification);
    return this;
  }

  public HeaderRow addEmptyColumn() {
    this.columns.add("");
    this.justifications.add(Justification.LEFT);
    return this;
  }

  public List<String> getColumns() {
    return columns;
  }

  List<Justification> getJustifications() {
    return justifications;
  }

  int getColumnCount() {
    return columns.size();
  }

  int getColumnWidth(int i) {
    return columns.get(i).length();
  }

  @Override
  public String toString() {
    return String.join(" | ", columns);
  }

}
