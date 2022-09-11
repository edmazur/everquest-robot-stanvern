package com.edmazur.eqrs.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataRow {

  private List<String> columns = new ArrayList<>();
  // This field technically leaks Discord implementation details, whereas these table classes are
  // meant to be agnostic to output format, but I haven't thought of a way of avoiding this that
  // doesn't introduce a bunch of unnecessary complexity.
  private Optional<Integer> maybeCodeFontEndIndex = Optional.empty();
  // These fields leak ToD table implementation details, whereas these table classes are meant to be
  // agnostic to output format, but I'm not sure that there's a good way around this.
  private Optional<Double> maybeProgressPercentage = Optional.empty();
  private Optional<Integer> maybeProgressWidth = Optional.empty();

  public DataRow addColumn(String column) {
    this.columns.add(column);
    return this;
  }

  public List<String> getColumns() {
    return columns;
  }

  public DataRow setCodeFontEndIndex(int codeFontEndIndex) {
    this.maybeCodeFontEndIndex = Optional.of(codeFontEndIndex);
    return this;
  }

  public Optional<Integer> getCodeFontEndIndex() {
    return maybeCodeFontEndIndex;
  }

  public DataRow setProgress(double progressPercentage, int progressWidth) {
    this.maybeProgressPercentage = Optional.of(progressPercentage);
    this.maybeProgressWidth = Optional.of(progressWidth);
    return this;
  }

  public Optional<Double> getProgressPercentage() {
    return maybeProgressPercentage;
  }

  public Optional<Integer> getProgressWidth() {
    return maybeProgressWidth;
  }

  int getColumnWidth(int i) {
    return columns.get(i).length();
  }

  @Override
  public String toString() {
    return String.join(", ", columns);
  }

}
