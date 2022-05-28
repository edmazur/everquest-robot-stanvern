package com.edmazur.eqrs.game;

import java.io.File;
import java.io.IOException;

public class GameScreenshotter {

  public File get() {
    File file = null;
    try {
      file = File.createTempFile(this.getClass().getName() + "-", ".png");
      Runtime.getRuntime().exec(
          new String[] {"import", "-window", "EverQuest", file.getAbsolutePath()});
      // Give some time for the screenshot to complete.
      // TODO: Check every 100ms or something for the file being ready.
      Thread.sleep(1000 * 5);
    } catch (IOException | InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return file;
  }

}
