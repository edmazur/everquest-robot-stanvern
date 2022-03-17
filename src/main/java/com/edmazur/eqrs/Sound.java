package com.edmazur.eqrs;

import java.io.IOException;

public class Sound {

  public void play() {
    try {
      Runtime.getRuntime().exec(new String[] {"mplayer", "/home/mazur/eclipse-workspace/RobotStanvern/audio/time-to-slay-the-dragon.mp3"});
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}