package com.edmazur.eqrs.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO: Add test cases for getActiveWindow().
class WindowTest {

  private Instant now;

  @BeforeEach
  void init() {
    now = Instant.now();
  }

  @Test
  void getStatusNow() {
    assertEquals(Window.Status.NOW, getNowWindow().getStatus(now));
  }

  @Test
  void getStatusSoon() {
    assertEquals(Window.Status.SOON, getSoonWindow().getStatus(now));
  }

  @Test
  void getStatusLater() {
    assertEquals(Window.Status.LATER, getLaterWindow().getStatus(now));
  }

  @Test
  void getStatusPast() {
    assertEquals(Window.Status.PAST, getPastWindow().getStatus(now));
  }

  @Test
  void getActiveWindowStatusNow() {
    List<Window> windows =
        List.of(getNowWindow(), getSoonWindow(), getLaterWindow(), getPastWindow());
    assertEquals(Window.Status.NOW, Window.getActiveWindowStatus(windows, now));
  }

  @Test
  void getActiveWindowStatusSoon() {
    List<Window> windows =
        List.of(getSoonWindow(), getLaterWindow(), getPastWindow());
    assertEquals(Window.Status.SOON, Window.getActiveWindowStatus(windows, now));
  }

  @Test
  void getActiveWindowStatusLater() {
    List<Window> windows =
        List.of(getLaterWindow(), getPastWindow());
    assertEquals(Window.Status.LATER, Window.getActiveWindowStatus(windows, now));
  }

  @Test
  void getActiveWindowStatusPast() {
    List<Window> windows =
        List.of(getPastWindow());
    assertEquals(Window.Status.PAST, Window.getActiveWindowStatus(windows, now));
  }

  private Window getNowWindow() {
    Instant start = now.minus(Duration.ofHours(1));
    Instant end = now.plus(Duration.ofHours(1));
    return new Window(start, end, 0);
  }

  private Window getSoonWindow() {
    Instant start = now.plus(Window.Status.SOON_THRESHOLD.dividedBy(2));
    Instant end = start.plus(Duration.ofHours(1));
    return new Window(start, end, 0);
  }

  private Window getLaterWindow() {
    Instant start = now.plus(Window.Status.SOON_THRESHOLD.multipliedBy(2));
    Instant end = start.plus(Duration.ofHours(1));
    return new Window(start, end, 0);
  }

  private Window getPastWindow() {
    Instant start = now.minus(Duration.ofHours(1));
    Instant end = now.minus(Duration.ofHours(2));
    return new Window(start, end, 0);
  }

}
