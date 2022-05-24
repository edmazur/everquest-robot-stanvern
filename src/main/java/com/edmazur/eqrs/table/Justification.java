package com.edmazur.eqrs.table;

public enum Justification {

  LEFT,
  RIGHT,
  ;

  public String getFormatString(int desiredWidth) {
    switch (this) {
      case LEFT:
        return "%-" + desiredWidth + "s";
      case RIGHT:
        return "%" + desiredWidth + "s";
      default:
        return null;
    }
  }

}
