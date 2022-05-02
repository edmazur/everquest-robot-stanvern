package com.edmazur.eqrs;

import java.io.IOException;

public class SoundPlayer {

  public enum Sound {

    DICE("/home/mazur/git/everquest-robot-stanvern/src/main/resources/dice.mp3"),

    ITS_TIME_TO_SLAY_THE_DRAGON(
        "/home/mazur/eclipse-workspace/RobotStanvern/audio/time-to-slay-the-dragon.mp3"),

    ;

    private final String path;

    private Sound(String path) {
      this.path = path;
    }

    private String getPath() {
      return path;
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
