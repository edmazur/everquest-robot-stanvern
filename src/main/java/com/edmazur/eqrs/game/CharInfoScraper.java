package com.edmazur.eqrs.game;

import com.edmazur.eqrs.Ocr;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CharInfoScraper {

  private final Ocr ocr;
  private final CharInfoOcrScrapeComparator charInfoOcrScrapeComparator;
  private final ExpPercentToNextLevelScraper expPercentToNextLevelScraper;

  public CharInfoScraper() {
    this.ocr = new Ocr();
    this.charInfoOcrScrapeComparator = new CharInfoOcrScrapeComparator();
    this.expPercentToNextLevelScraper = new ExpPercentToNextLevelScraper();
  }

  public CharInfo scrape(File image) {
    CharInfo charInfo = new CharInfo();

    // Scrape class/level/name.
    List<String> lines = ocr.scrape(image, charInfoOcrScrapeComparator);
    for (int i = 0; i < lines.size(); i++) {
      String line = cleanseOcrLine(lines.get(i));
      String[] parts = line.split("\\s");
      if (parts.length > 1) {
        String candidateText = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        Optional<EqClass> maybeEqClass = EqClass.fromAnyName(candidateText);
        if (maybeEqClass.isPresent()) {
          // EQ class has been found, so now we know where we are.
          charInfo.setEqClass(maybeEqClass.get());

          // Get level from the left on the current line.
          charInfo.setLevel(Integer.parseInt(parts[0]));

          // Get name from the first non-blank line above (it's usually the line right above, but
          // occasionally not).
          for (int j = 0; j < i; j++) {
            if (!lines.get(j).isBlank()) {
              charInfo.setName(cleanseOcrLine(lines.get(j)));
              break;
            }
          }
        }
      }
    }

    // Scrape exp.
    Optional<Integer> maybeExpPercentToNextLevel = expPercentToNextLevelScraper.scrape(image);
    if (maybeExpPercentToNextLevel.isPresent()) {
      charInfo.setExpPercentToNextLevel(maybeExpPercentToNextLevel.get());
    }

    return charInfo;
  }

  // Strips out everything except alphanumeric characters, whitespace characters (though
  // leading/trailing are removed), and "/". Everything else is assumed to be an OCR scrape error.
  // Examples seen:
  // - "Magician’" -> "Magician"
  // - "Agnostic," -> "Agnostic"
  // - "NEXT LEVEL," -> "NEXT LEVEL"
  // - "Raluca " -> "Raluca"
  private String cleanseOcrLine(String line) {
    return line.replaceAll("[^A-Za-z0-9\\s/]", "").trim();
  }

}
