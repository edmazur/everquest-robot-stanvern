package com.edmazur.eqrs.game.listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edmazur.eqrs.game.GameLogEvent;

/**
 * Reports false positive/negative rate for GameTodDetector based on a known
 * data set.
 */
public class GameTodDetectorBenchmark {

  private static final File EXPECTED_PRESENT = new File(
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/game-log-202202-tods-present.txt");
  private static final File EXPECTED_ABSENT = new File(
      "/home/mazur/git/everquest-robot-stanvern/src/test/resources/game-log-202202-tods-absent.txt");

  private static final Pattern LINE_PATTERN =
      Pattern.compile("^\\[(.+?)\\] (.+)$");
  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");

  private static final int EXAMPLES_TO_PRINT = 10;

  public static void main(String[] args) {
    List<GameLogEvent> expectedPositives = getGameLogEvents(EXPECTED_PRESENT);
    List<GameLogEvent> expectedNegatives = getGameLogEvents(EXPECTED_ABSENT);

    List<GameLogEvent> falseNegatives = runBenchmark(expectedPositives, true);
    List<GameLogEvent> falsePositives = runBenchmark(expectedNegatives, false);

    double falseNegativeRate = 100 - (double) falseNegatives.size() / expectedPositives.size() * 100;
    double falsePositiveRate = 100 - (double) falsePositives.size() / expectedNegatives.size() * 100;

    System.out.println("Examples that were expected to be recognized as having ToDs present:"
        + getExamplesForPrinting(falseNegatives));
    System.out.println();
    System.out.println("Examples that were expected to be recognized as having ToDs absent:"
        + getExamplesForPrinting(falsePositives));
    System.out.println();

    System.out.println(String.format("Accuracy when expecting ToDs: %.2f%% (%d/%d)",
        falseNegativeRate, expectedPositives.size() - falseNegatives.size(), expectedPositives.size()));
    System.out.println(String.format("Accuracy when not expecting ToDs: %.2f%% (%d/%d)",
        falsePositiveRate, expectedNegatives.size() - falsePositives.size(), expectedNegatives.size()));
  }

  private static List<GameLogEvent> getGameLogEvents(File file) {
    List<String> lines = null;
    try {
      lines = Files.readAllLines(file.toPath());
    } catch (IOException e) {
      System.err.println("Error running benchmark");
      e.printStackTrace();
      System.exit(-1);
    }

    List<GameLogEvent> gameLogEvents = new ArrayList<>(lines.size());
    for (String line : lines) {
      gameLogEvents.add(getGameLogEvent(line));
    }
    return gameLogEvents;
  }

  private static List<GameLogEvent> runBenchmark(List<GameLogEvent> gameLogEvents, boolean expectingTod) {
    GameTodDetector gameTodDetector = new GameTodDetector();
    List<GameLogEvent> incorrectDetections = new ArrayList<>();
    for (GameLogEvent gameLogEvent : gameLogEvents) {
      if (gameTodDetector.containsTod(gameLogEvent) != expectingTod) {
        incorrectDetections.add(gameLogEvent);
      }
    }
    return incorrectDetections;
  }

  private static GameLogEvent getGameLogEvent(String line) {
    Matcher matcher = LINE_PATTERN.matcher(line);
    matcher.matches();
    LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMAT);
    String payload = matcher.group(2);
    return new GameLogEvent(line, timestamp, payload);
  }

  private static String getExamplesForPrinting(List<GameLogEvent> gameLogEvents) {
    StringBuilder sb = new StringBuilder();
    for (GameLogEvent gameLogEvent :
        gameLogEvents.subList(0, Integer.min(gameLogEvents.size(), EXAMPLES_TO_PRINT))) {
      sb.append("\n- " + gameLogEvent.getFullLogLine());
    }
    return sb.toString();
  }

}