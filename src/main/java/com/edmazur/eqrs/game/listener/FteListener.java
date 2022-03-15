package com.edmazur.eqrs.game.listener;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordUser;
import com.edmazur.eqrs.game.GameLogEvent;

public class FteListener implements GameLogListener {

  private static final boolean SEND_DISCORD_MESSAGE = true;
  private static final boolean SEND_DISCORD_MESSAGE_AS_DM = true;
  private static final boolean PLAY_SOUND = true;

  private static final Pattern FTE_PATTERN = Pattern.compile(".+ engages \\w+!");

  private final Discord discord;

  public FteListener(Discord discord) {
    this.discord = discord;
  }

  @Override
  public String getConfig() {
    return String.format(
        "SEND_DISCORD_MESSAGE=%s, SEND_DISCORD_MESSAGE_AS_DM=%s, PLAY_SOUND=%s",
        SEND_DISCORD_MESSAGE, SEND_DISCORD_MESSAGE_AS_DM, PLAY_SOUND);
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    Matcher matcher = FTE_PATTERN.matcher(gameLogEvent.getText());
    if (matcher.matches()) {
      if (PLAY_SOUND) {
        try {
          Runtime.getRuntime().exec(new String[] {"mplayer", "/home/mazur/eclipse-workspace/RobotStanvern/audio/time-to-slay-the-dragon.mp3"});
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      if (SEND_DISCORD_MESSAGE) {
        String message = "FTE notice! ET: `" + gameLogEvent.getFullLogLine() + "`";
        if (SEND_DISCORD_MESSAGE_AS_DM) {
          discord.sendMessage(DiscordUser.EDMAZUR, message);
        } else {
          discord.sendMessage(DiscordChannel.RAIDER_CHAT, message);
        }
      }
    }
  }

}