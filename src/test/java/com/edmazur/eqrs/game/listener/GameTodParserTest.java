package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.edmazur.eqlp.EqLogEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// TODO: Since this test depends on data external to the repo, it can break when that data changes.
// Ideally, rather than rely on external data (target names/aliases in this case), the repo ought to
// be entirely self-contained.
class GameTodParserTest {

  private static final File TEST_CASE_FILE = new File("src/test/resources/logs/tods-annotated.txt");

  private static GameTodParser gameTodParser;
  private static GameTodDetector gameTodDetector;

  @BeforeAll
  static void beforeAll() {
    // TODO: Maybe stub out RaidTargets. The current setup has an external dependency, making these
    // unit tests (a) not hermetic and (b) more heavyweight than they ought to be.
    gameTodParser = new GameTodParser();
    gameTodDetector = new GameTodDetector();
  }

  private static List<Arguments> provideTestCases() throws IOException {
    List<Arguments> arguments = new ArrayList<>();
    for (String line : Files.readAllLines(TEST_CASE_FILE.toPath())) {
      arguments.add(Arguments.of(line));
    }
    return arguments;
  }

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void parse(String testCase) {
    String[] parts = testCase.split(" # ");
    String input = parts[0];
    String expectedOutput = parts[1];
    boolean expectingSuccessfulParse = !expectedOutput.startsWith("Error: ");
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(input).get();
    GameTodParseResult parseResult =
        gameTodParser.parse(eqLogEvent, gameTodDetector.getTodMessage(eqLogEvent).get());
    if (expectingSuccessfulParse && !parseResult.wasSuccessfullyParsed()) {
      fail("Expected to be able to parse: " + input);
    } else if (!expectingSuccessfulParse && parseResult.wasSuccessfullyParsed()) {
      fail("Did not expect to be able to parse: " + input + " (parsed as "
          + parseResult.getRaidTarget().getName() + ")");
    } else if (!expectingSuccessfulParse) {
      String expectedError = expectedOutput.split("Error: ")[1];
      assertEquals(expectedError, parseResult.getError(), "Error parsing: " + input);
    } else if (expectingSuccessfulParse) {
      assertEquals(expectedOutput, parseResult.getRaidTarget().getName(),
          "Error parsing: " + input);
    }
  }

}
