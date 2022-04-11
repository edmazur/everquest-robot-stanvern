package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.RateLimiter;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameLogEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

public class RaidTargetSpawnListener implements GameLogListener {

  private static final Boolean BATPHONE = false;

  private static final String REGULAR_MESSAGE =
      "@everyone %s POP! Stanvern's stream should be up if you want to confirm. "
      + "Stay tuned for a decision from an officer on whether we will contest it. Be ready in case "
      + "we do!\n"
      + "\n"
      + "(Disclaimer: This is an automated message, brought to you by S.E.B.S. (Stanvern Emergency "
      + "Broadcast System). I'm just a dumb robot and I'm really sorry if I made a mistake!)";

  private static final String BATPHONE_MESSAGE =
      "@everyone %s POP! Go go go! Stanvern's stream should be up if you want to confirm.\n"
      + "\n"
      + "(Disclaimer: This is an automated message, brought to you by S.E.B.S. (Stanvern Emergency "
      + "Broadcast System). I'm just a dumb robot and I'm really sorry if I made a mistake!)";

  private static Map<String, String> TRIGGERS_AND_TARGETS = Map.ofEntries(
      Map.entry("Master Yael begins to cast a spell.", "YAEL"));

  private static final Duration RATE_LIMIT = Duration.ofHours(24);

  private final Discord discord;

  // TODO: Make this a per-target rate limiter if/when you add more targets.
  private final RateLimiter rateLimiter = new RateLimiter(RATE_LIMIT);

  public RaidTargetSpawnListener(Discord discord) {
    this.discord = discord;
  }

  @Override
  public String getConfig() {
    return String.format("BATPHONE=%s", BATPHONE);
  }

  @Override
  public void onGameLogEvent(GameLogEvent gameLogEvent) {
    for (Map.Entry<String, String> mapEntry : TRIGGERS_AND_TARGETS.entrySet()) {
      String trigger = mapEntry.getKey();
      String target = mapEntry.getValue();
      // Use startsWith() instead of equals() to account for potential end-of-line weirdness
      // (trailing whitespace, /r, etc.).
      if (gameLogEvent.getText().startsWith(trigger)) {
        if (rateLimiter.getPermission()) {
          if (BATPHONE) {
            discord.sendMessage(
                DiscordChannel.RAID_BATPHONE,
                String.format(BATPHONE_MESSAGE, target),
                getGameScreenshot());
          } else {
            discord.sendMessage(
                DiscordChannel.RAIDER_CHAT,
                String.format(REGULAR_MESSAGE, target),
                getGameScreenshot());
          }
          break;
        }
      }
    }

  }

  private File getGameScreenshot() {
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
