package com.edmazur.eqrs.game;

import java.util.Comparator;
import java.util.List;

public class CharInfoOcrScrapeComparator implements Comparator<List<String>> {

  private static final List<String> KEYWORDS = List.of(
      "HP",
      "AC",
      "ATK",
      "NEXT LEVEL");

  private static int getKeywordRecognitionCount(List<String> lines) {
    int keywordRecognitionCount = 0;
    for (String line : lines) {
      for (String keyword : KEYWORDS) {
        if (line.contains(keyword)) {
          keywordRecognitionCount++;
        }
      }
    }
    return keywordRecognitionCount;
  }

  private static int getNonEmptyLineCount(List<String> lines) {
    int nonEmptyLineCount = 0;
    for (String line : lines) {
      if (!line.isBlank()) {
        nonEmptyLineCount++;
      }
    }
    return nonEmptyLineCount;
  }

  public static String getDebugInfo(List<String> lines) {
    int keywordRecognitionCount = getKeywordRecognitionCount(lines);
    int nonEmptyLineCount = getNonEmptyLineCount(lines);
    int emptyLineCount = lines.size() - nonEmptyLineCount;
    return String.format("keywordRecognitionCount=%d, nonEmptyLineCount=%d, emptyLineCount=%d",
        keywordRecognitionCount, nonEmptyLineCount, emptyLineCount);
  }

  @Override
  // TODO: Simplify this with Guava comparator utility.
  public int compare(List<String> a, List<String> b) {
    // These are final just to avoid the LocalVariableName checkstyle check that applies to
    // non-final local variables (it doesn't like the single uncapitalized leading characters).
    final int aKeywordRecognitionCount = getKeywordRecognitionCount(a);
    final int bKeywordRecognitionCount = getKeywordRecognitionCount(b);
    final int aNonEmptyLineCount = getNonEmptyLineCount(a);
    final int bNonEmptyLineCount = getNonEmptyLineCount(b);

    if (aKeywordRecognitionCount == bKeywordRecognitionCount) {
      return aNonEmptyLineCount - bNonEmptyLineCount;
    } else {
      return aKeywordRecognitionCount - bKeywordRecognitionCount;
    }
  }

}
