package com.edmazur.eqrs;

import java.io.IOException;

public class SoundPlayer {

  private static final String SOUND_PATH = "src/main/resources";

  public enum Sound {

    DICE("dice.mp3"),
    ITS_TIME_TO_SLAY_THE_DRAGON("its-time-to-slay-the-dragon.mp3"),

    ;

    private final String path;

    private Sound(String path) {
      this.path = path;
    }

    private String getPath() {
      return SOUND_PATH + "/" + path;
    }

  }

  public void play(Sound sound) {
    try {
      Runtime.getRuntime().exec(new String[] {"mplayer", sound.getPath()});
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
