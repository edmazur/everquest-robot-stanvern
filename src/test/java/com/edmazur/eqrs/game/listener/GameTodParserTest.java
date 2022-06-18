package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Json;
import com.edmazur.eqrs.game.RaidTargets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameTodParserTest {

  private static final File TEST_CASE_FILE = new File(
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/logs/202202-tods-annotated.txt");

  private static GameTodParser gameTodParser;

  @BeforeAll
  static void init() {
    // TODO: Maybe stub out RaidTargets. The current setup has an external dependency, making these
    // unit tests (a) not hermetic and (b) more heavyweight than they ought to be.
    Config config = new Config();
    Json json = new Json();
    RaidTargets raidTargets = new RaidTargets(config, json);
    gameTodParser = new GameTodParser(raidTargets);
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
    boolean expectingSuccessfulParse = !expectedOutput.equals("-");
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(input).get();
    Optional<GameTodParseResult> parseResult = gameTodParser.parse(eqLogEvent);
    if (expectingSuccessfulParse && !parseResult.isPresent()) {
      fail("Expected to be able to parse: " + input);
    } else if (!expectingSuccessfulParse && parseResult.isPresent()) {
      fail("Did not expect to be able to parse: " + input);
    } else if (expectingSuccessfulParse) {
      assertEquals(expectedOutput, parseResult.get().getRaidTarget().getName(),
          "Error parsing: " + input);
    }
  }

}
