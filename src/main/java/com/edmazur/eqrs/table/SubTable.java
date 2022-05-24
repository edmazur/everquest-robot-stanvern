package com.edmazur.eqrs.table;

import java.util.ArrayList;
import java.util.List;

public class SubTable {

  private String heading;
  private HeaderRow headerRow;
  private List<DataRow> dataRows = new ArrayList<>();

  public SubTable setHeading(String heading) {
    this.heading = heading;
    return this;
  }

  public String getHeading() {
    return heading;
  }

  public SubTable setHeaderRow(HeaderRow headerRow) {
    this.headerRow = headerRow;
    return this;
  }

  public HeaderRow getHeaderRow() {
    return headerRow;
  }

  List<Justification> getJustifications() {
    return headerRow.getJustifications();
  }

  public SubTable addDataRow(DataRow dataRow) {
    this.dataRows.add(dataRow);
    return this;
  }

  public List<DataRow> getDataRows() {
    return dataRows;
  }

  int getColumnCount() {
    return headerRow.getColumnCount();
  }

  int getMaxColumnWidth(int i) {
    int subTableMaxColumnWidth = headerRow.getColumnWidth(i);
    for (DataRow dataRow : dataRows) {
      int dataRowColumnWidth = dataRow.getColumnWidth(i);
      subTableMaxColumnWidth = Math.max(subTableMaxColumnWidth, dataRowColumnWidth);
    }
    return subTableMaxColumnWidth;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(heading + "\n");
    sb.append(headerRow + "\n");
    sb.append("-----\n");
    for (DataRow dataRow : dataRows) {
      sb.append(dataRow + "\n");
    }
    return sb.toString();
  }

}
