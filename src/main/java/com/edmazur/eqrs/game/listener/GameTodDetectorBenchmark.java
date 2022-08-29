// $ ./gradlew runGameTodDetectorBenchmark

package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Reports false positive/negative rate for GameTodDetector based on a known
 * data set.
 */
public class GameTodDetectorBenchmark {

  private static final File EXPECTED_PRESENT = new File(
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/logs/tods-present.txt");
  private static final File EXPECTED_ABSENT = new File(
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/logs/tods-absent.txt");

  private static final int EXAMPLES_TO_PRINT = 10;

  public static void main(String[] args) {
    List<EqLogEvent> expectedPositives = getGameLogEvents(EXPECTED_PRESENT);
    List<EqLogEvent> expectedNegatives = getGameLogEvents(EXPECTED_ABSENT);

    List<EqLogEvent> falseNegatives = runBenchmark(expectedPositives, true);
    List<EqLogEvent> falsePositives = runBenchmark(expectedNegatives, false);

    double falseNegativeRate =
        100 - (double) falseNegatives.size() / expectedPositives.size() * 100;
    double falsePositiveRate =
        100 - (double) falsePositives.size() / expectedNegatives.size() * 100;

    System.out.println("Examples that were expected to be recognized as having ToDs present:"
        + getExamplesForPrinting(falseNegatives));
    System.out.println();
    System.out.println("Examples that were expected to be recognized as having ToDs absent:"
        + getExamplesForPrinting(falsePositives));
    System.out.println();

    System.out.println(String.format("Accuracy when expecting ToDs: %.2f%% (%d/%d)",
        falseNegativeRate,
        expectedPositives.size() - falseNegatives.size(),
        expectedPositives.size()));
    System.out.println(String.format("Accuracy when not expecting ToDs: %.2f%% (%d/%d)",
        falsePositiveRate,
        expectedNegatives.size() - falsePositives.size(),
        expectedNegatives.size()));
  }

  private static List<EqLogEvent> getGameLogEvents(File file) {
    List<String> lines = null;
    try {
      lines = Files.readAllLines(file.toPath());
    } catch (IOException e) {
      System.err.println("Error running benchmark");
      e.printStackTrace();
      System.exit(-1);
    }

    List<EqLogEvent> eqLogEvents = new ArrayList<>(lines.size());
    for (String line : lines) {
      eqLogEvents.add(EqLogEvent.parseFromLine(line).get());
    }
    return eqLogEvents;
  }

  private static List<EqLogEvent> runBenchmark(List<EqLogEvent> eqLogEvents, boolean expectingTod) {
    GameTodDetector gameTodDetector = new GameTodDetector();
    List<EqLogEvent> incorrectDetections = new ArrayList<>();
    for (EqLogEvent eqLogEvent : eqLogEvents) {
      if (gameTodDetector.getTodMessage(eqLogEvent).isPresent() != expectingTod) {
        incorrectDetections.add(eqLogEvent);
      }
    }
    return incorrectDetections;
  }

  private static String getExamplesForPrinting(List<EqLogEvent> eqLogEvents) {
    StringBuilder sb = new StringBuilder();
    for (EqLogEvent eqLogEvent :
        eqLogEvents.subList(0, Integer.min(eqLogEvents.size(), EXAMPLES_TO_PRINT))) {
      sb.append("\n- " + eqLogEvent.getFullLine());
    }
    return sb.toString();
  }

}
