package com.edmazur.eqrs.game;

public class Item {

  private final String name;
  private final String url;

  public Item(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public String getNameEscaped() {
    return escape(name);
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return name + " (" + url + ")";
  }

  // Make apostrophes and backticks interchangeable:
  // - Makes it easier for one-off manual lookups.
  // - NinjaLooter replaces backticks with apostrophes.
  public static String escape(String s) {
    return s.replace("`", "'");
  }

}
