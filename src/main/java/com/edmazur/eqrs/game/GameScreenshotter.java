package com.edmazur.eqrs.game;

import com.edmazur.eqrs.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameScreenshotter {

  private static final Logger LOGGER = new Logger();

  private static final String GAME_CLIENT_WINDOW_TITLE = "EverQuest";
  private static final BigDecimal GAMMA_CORRECTION = new BigDecimal("2");

  public Optional<File> get() {
    try {
      // Save game client position before making any changes.
      String gameClientPositionBeforeActivating = getGameClientPosition();
      int desktopBeforeActivating = getCurrentDesktop();

      // If the game client window is on a different desktop than what's currently in use OR if it's
      // minimized, then the screenshot will be stale (i.e. it'll be whatever was last rendered when
      // the window was visible). Avoid this problem by activating the window, which will switch to
      // its desktop and un-minimize if needed.
      activateGameClient();

      // Take screenshot.
      File rawScreenshot = getScreenshot();

      // Re-minimize game client if needed.
      String gameClientPositionAfterActivating = getGameClientPosition();
      boolean gameClientWasMinimizedBeforeActivating =
          !gameClientPositionBeforeActivating.equals(gameClientPositionAfterActivating);
      if (gameClientWasMinimizedBeforeActivating) {
        minimizeGameClient();
      }

      // Switch back to original desktop if needed.
      setDesktop(desktopBeforeActivating);

      // Correct screenshot.
      File correctedScreenshot = increaseGamma(rawScreenshot);

      return Optional.of(correctedScreenshot);
    } catch (IOException | InterruptedException e) {
      LOGGER.log("Error getting screenshot");
      e.printStackTrace();
      return Optional.empty();
    }
  }

  private String getGameClientPosition() throws IOException {
    Process process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c",
        "xdotool search --name ^" + GAME_CLIENT_WINDOW_TITLE + "$ getwindowgeometry"
            + " | grep Position"});
    return new BufferedReader(new InputStreamReader(
        process.getInputStream())).lines().collect(Collectors.toList()).get(0);
  }

  private int getCurrentDesktop() throws IOException {
    Process process = Runtime.getRuntime().exec(new String[] {"xdotool", "get_desktop"});
    return Integer.parseInt(new BufferedReader(new InputStreamReader(
        process.getInputStream())).lines().collect(Collectors.toList()).get(0));
  }

  private void activateGameClient() throws IOException, InterruptedException {
    Runtime.getRuntime().exec(
        new String[] {"xdotool", "search", "--name", "^" + GAME_CLIENT_WINDOW_TITLE + "$",
            "windowactivate", "--sync"}).waitFor();
  }

  private File getScreenshot() throws IOException, InterruptedException {
    File file = File.createTempFile(this.getClass().getName() + "-", ".png");
    Runtime.getRuntime().exec(
        new String[] {"import", "-window", GAME_CLIENT_WINDOW_TITLE,
            file.getAbsolutePath()}).waitFor();
    return file;
  }

  private File increaseGamma(File input)
      throws IOException, InterruptedException {
    File output = File.createTempFile(this.getClass().getName() + "-", ".png");
    Runtime.getRuntime().exec(
        new String[] {"convert", input.getAbsolutePath(), "-gamma", GAMMA_CORRECTION.toString(),
            output.getAbsolutePath()}).waitFor();
    return output;
  }

  private void minimizeGameClient() throws IOException {
    Runtime.getRuntime().exec(
        new String[] {"xdotool", "search", "--name", "^" + GAME_CLIENT_WINDOW_TITLE + "$",
            "windowminimize"});
  }

  private void setDesktop(int desktop) throws IOException {
    Runtime.getRuntime().exec(new String[] {"xdotool", "set_desktop", Integer.toString(desktop)});
  }

}
