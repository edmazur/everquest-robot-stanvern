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

  private static final String IMAGE_DIRECTORY = "src/test/resources/screenshots";

  private CharInfoScraper charInfoScraper;

  @BeforeEach
  void init() {
    Ocr ocr = new Ocr();
    CharInfoOcrScrapeComparator charInfoOcrScrapeComparator = new CharInfoOcrScrapeComparator();
    // TODO: Replace this with a mock object since it has its own unit tests.
    ExpPercentToNextLevelScraper expPercentToNextLevelScraper = new ExpPercentToNextLevelScraper();
    charInfoScraper =
        new CharInfoScraper(ocr, charInfoOcrScrapeComparator, expPercentToNextLevelScraper);
  }

  private static List<Arguments> provideWorkingCases() {
    return List.of(
        Arguments.of(
            "cropped-delrake-60-58.png",
            new CharInfo()
                .setName("Delrake")
                .setEqClass(EqClass.MONK)
                .setLevel(60)
                .setExpPercentToNextLevel(58)),

        Arguments.of(
            "cropped-klearic-60-96.png",
            new CharInfo()
                .setName("Klearic")
                .setEqClass(EqClass.CLERIC)
                .setLevel(60)
                .setExpPercentToNextLevel(96)),

        Arguments.of(
            "cropped-raluca-60-84-a.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-b.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-c.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-d.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-e.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-f.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-g.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-h.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-raluca-60-84-i.png",
            new CharInfo()
                .setName("Raluca")
                .setEqClass(EqClass.ENCHANTER)
                .setLevel(60)
                .setExpPercentToNextLevel(84)),

        Arguments.of(
            "cropped-siuaen-13-62.png",
            new CharInfo()
                .setName("Siuaen")
                .setEqClass(EqClass.WIZARD)
                .setLevel(13)
                .setExpPercentToNextLevel(62)),

        Arguments.of(
            "cropped-wilhelme-13-23.png",
            new CharInfo()
                .setName("Wilhelme")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(13)
                .setExpPercentToNextLevel(23)),

        Arguments.of(
            "cropped-wilma-1-42.png",
            new CharInfo()
                .setName("Wilma")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(1)
                .setExpPercentToNextLevel(42)),

        Arguments.of(
            "cropped-wilma-13-13.png",
            new CharInfo()
                .setName("Wilma")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(13)
                .setExpPercentToNextLevel(13)),

        Arguments.of(
            "cropped-wilma-2-0.png",
            new CharInfo()
                .setName("Wilma")
                .setEqClass(EqClass.MAGICIAN)
                .setLevel(2)
                .setExpPercentToNextLevel(0))
        );
  }


  private static List<Arguments> provideBrokenCases() {
    return List.of(
        Arguments.of(
            "cropped-stanvern-60-65.png",
            new CharInfo()
                // Should be "Stanvern".
                .setName("Stanyern")
                .setEqClass(EqClass.RANGER)
                .setLevel(60)
                .setExpPercentToNextLevel(65))
        );
  }

  @ParameterizedTest
  @MethodSource("provideWorkingCases")
  void workingCases(String imageName, CharInfo expectedCharInfo) {
    runTest(imageName, expectedCharInfo);
  }

  @ParameterizedTest
  @MethodSource("provideBrokenCases")
  void brokenCases(String imageName, CharInfo expectedCharInfo) {
    runTest(imageName, expectedCharInfo);
  }

  private void runTest(String imageName, CharInfo expectedCharInfo) {
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
