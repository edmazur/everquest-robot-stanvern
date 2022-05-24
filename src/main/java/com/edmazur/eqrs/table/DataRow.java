package com.edmazur.eqrs.table;

import java.util.ArrayList;
import java.util.List;

public class DataRow {

  private List<String> columns = new ArrayList<>();
  // This field technically leaks Discord implementation details, whereas these table classes are
  // meant to be agnostic to output format, but I haven't thought of a way of avoiding this that
  // doesn't introduce a bunch of unnecessary complexity.
  private int codeFontEndIndex;

  public DataRow addColumn(String column) {
    this.columns.add(column);
    return this;
  }

  public List<String> getColumns() {
    return columns;
  }

  public DataRow setCodeFontEndIndex(int codeFontEndIndex) {
    this.codeFontEndIndex = codeFontEndIndex;
    return this;
  }

  public int getCodeFontIndex() {
    return codeFontEndIndex;
  }

  int getColumnWidth(int i) {
    return columns.get(i).length();
  }

  @Override
  public String toString() {
    return String.join(", ", columns);
  }

}
