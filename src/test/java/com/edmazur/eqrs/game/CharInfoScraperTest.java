package com.edmazur.eqrs.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.edmazur.eqrs.Ocr;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CharInfoScraperTest {

  private static final String IMAGE_DIRECTORY =
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/screenshots";

  private CharInfoScraper charInfoScraper;

  @BeforeEach
  void init() {
    Ocr ocr = new Ocr();
    // TODO: Replace this with a mock object since it has its own unit tests.
    ExpPercentToNextLevelScraper expPercentToNextLevelScraper = new ExpPercentToNextLevelScraper();
    charInfoScraper = new CharInfoScraper(ocr, expPercentToNextLevelScraper);
  }

  private static List<Arguments> provideTestCases() {
    return List.of(
        Arguments.of(
            "cropped-klearic-60-96.png",
            new CharInfo()
                .setName("Klearic")
                .setEqClass(EqClass.CLERIC)
                .setLevel(60)
                .setExpPercentToNextLevel(96)),

        Arguments.of(
            "cropped-wilma-1-42.png",
            new CharInfo()
                .setName("Wilma")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(1)
                .setExpPercentToNextLevel(42)),

        Arguments.of(
            "cropped-wilma-2-0.png",
            new CharInfo()
                .setName("Wilma")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(2)
                .setExpPercentToNextLevel(0)));
  }

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void expectedCharInfoMatchesActual(String imageName, CharInfo expectedCharInfo) {
    CharInfo actualCharInfo = charInfoScraper.scrape(new File(IMAGE_DIRECTORY + "/" + imageName));
    assertEquals(expectedCharInfo.getName(), actualCharInfo.getName(), "name");
    assertEquals(expectedCharInfo.getEqClass(), actualCharInfo.getEqClass(), "class");
    assertEquals(expectedCharInfo.getLevel(), actualCharInfo.getLevel(), "level");
    assertEquals(
        expectedCharInfo.getExpPercentToNextLevel(),
        actualCharInfo.getExpPercentToNextLevel(),
        "exp");
  }

}
