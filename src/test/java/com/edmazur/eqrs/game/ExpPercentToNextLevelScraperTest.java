package com.edmazur.eqrs.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpPercentToNextLevelScraperTest {

  private static final String IMAGE_DIRECTORY =
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/screenshots";

  private ExpPercentToNextLevelScraper expPercentToNextLevelScraper;

  @BeforeEach
  void init() {
    expPercentToNextLevelScraper = new ExpPercentToNextLevelScraper();
  }

  // TODO: Add the cropped screenshots here too. Currently they are all tested indirectly in
  // CharInfoScraperTest, but that should eventually be mocked out, at which point they'll need to
  // be tested here.
  @ParameterizedTest
  @CsvSource({
    "uncropped-stanvern-51-0.png,0",
    "uncropped-stanvern-52-20-a.png,20",
    "uncropped-stanvern-52-20-b.png,20",
    "uncropped-stanvern-52-21.png,21",
    "uncropped-stanvern-52-64.png,64",
  })
  void expectedExpMatchesActual(String imageName, String expectedExp) {
    Optional<Integer> actualExp =
        expPercentToNextLevelScraper.scrape(new File(IMAGE_DIRECTORY + "/" + imageName));
    if (actualExp.isEmpty()) {
      fail("Could not get exp from image: " + imageName);
    }
    assertEquals(
        Integer.parseInt(expectedExp), actualExp.get(),
        "Expected exp for " + imageName + " did not match actual");
  }

}
