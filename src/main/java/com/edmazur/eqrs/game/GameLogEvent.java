package com.edmazur.eqrs.game;

import java.time.LocalDateTime;

public class GameLogEvent {

  private final String fullLogline;
  private final LocalDateTime time;
  private final String text;

  public GameLogEvent(String fullLogLine, LocalDateTime time, String text) {
    this.fullLogline = fullLogLine;
    this.time = time;
    this.text = text;
  }

  public String getFullLogLine() {
    return fullLogline;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public String getText() {
    return text;
  }

}