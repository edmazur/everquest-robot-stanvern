package com.edmazur.eqrs.game;

import java.util.regex.Pattern;

/**
 * Note that backticks (`) and apostrophes (') make item name handling a bit more complicated:
 * - Both are used in-game.
 * - Both are used on the wiki.
 * - The same item can have non-matching usage in-game vs. on the wiki.
 * - The wiki name is this class's source-of-truth.
 * - Discord treats backticks as a formatting instruction.
 * - Some programmatic consumers of these names (e.g. GG's Alfred) expect no backticks.
 * Keep this logic in sync with the logic in ItemDatabase class.
 */
public class Item {

  private final String name;
  private final String url;

  public Item(String name, String url) {
    this.name = name;
    this.url = url;
  }

  /**
   * Gets a pattern that will match names in a way to avoids backtick/apostrophe issues.
   * - Pattern is case-insensitive.
   * - Can match against multi-line input.
   * - Optional wildcards are included on both sides because there are no use cases where you want
   *   to match exactly/only the item.
   * - The item name is grouped in case callers need exactly what was matched.
   */
  public Pattern getNamePattern() {
    StringBuilder sb = new StringBuilder();
    sb.append(".*");
    sb.append("(");
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      switch (c) {
        case '\'':
        case '`':
          sb.append("(\\'|\\`)");
          break;
        default:
          sb.append(c);
      }
    }
    sb.append(")");
    sb.append(".*");
    return Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  }

  /**
   * This returns the raw wiki name.
   */
  public String getName() {
    return name;
  }

  /**
   * Note that this should be used only when something is not expecting backticks.
   * In particular, it's not needed for Discord format escaping - use double backticks for that.
   */
  public String getNameWithBackticksReplaced() {
    return name.replace('`', '\'');
  }

  public String getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return name + " (" + url + ")";
  }

}
