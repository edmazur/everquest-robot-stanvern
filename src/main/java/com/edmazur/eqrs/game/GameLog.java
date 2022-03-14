package com.edmazur.eqrs.game;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

public class GameLog extends TailerListenerAdapter implements Iterable<GameLogEvent>, Iterator<GameLogEvent> {

  private static final String LOG_DIRECTORY_PREFIX = "/opt/everquest/EverQuest Project 1999/Logs/";
  private static final Duration RECENCY_THRESHOLD = Duration.ofMinutes(1);
  private static final Pattern LINE_PATTERN = Pattern.compile("^\\[(.+?)\\] (.+)$");
  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");

  private final BlockingQueue<GameLogEvent> queue = new LinkedBlockingQueue<GameLogEvent>();

  public GameLog(String characterName) {
    String pathToGameLog = LOG_DIRECTORY_PREFIX + "eqlog_" + characterName + "_P1999Green.txt";
    // 100 = 100ms delay between checks of the file for new contents.
    // true = Start from end of file.
    Tailer tailer = new Tailer(new File(pathToGameLog), this, 100, true);
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    executorService.execute(tailer);
  }

  @Override
  public void handle(String line) {
    // Ignore empty lines. These appear sporadically through the log, I think
    // maybe when doing /who commands...for GMs?
    if (line.equals("")) {
      return;
    }

    // Extract basic line info.
    LocalDateTime time;
    String text;
    Matcher matcher = LINE_PATTERN.matcher(line);
    if (matcher.matches() && matcher.groupCount() == 2) {
      try {
        time = LocalDateTime.parse(matcher.group(1), TIMESTAMP_FORMAT);
      } catch (DateTimeParseException e) {
        System.err.println(e);
        return;
      }

      // Sometimes the log tailing library loops back to the beginning for some
      // reason, so check the timestamp to make sure it's recent.
      if (Duration.between(time, LocalDateTime.now()).compareTo(RECENCY_THRESHOLD) > 0) {
        return;
      }

      text = matcher.group(2);
      GameLogEvent gameLogEvent = new GameLogEvent(line, time, text);
      try {
        queue.put(gameLogEvent);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("Error parsing line: " + line);
    }
  }

  @Override
  public void handle(Exception e) {
    e.printStackTrace();
  }

  @Override
  public Iterator<GameLogEvent> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public GameLogEvent next() {
    try {
      return queue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }

}