package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.RateLimiter;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameScreenshotter;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RaidTargetSpawnListener implements EqLogListener {

  private static final Boolean BATPHONE = false;

  private static final List<DiscordChannel> PROD_BATPHONE_CHANNELS = List.of(
      DiscordChannel.FOW_RAID_BATPHONE,
      DiscordChannel.GG_BATPHONE);
  private static final List<DiscordChannel> PROD_NONBATPHONE_CHANNELS = List.of(
      DiscordChannel.FOW_RAIDER_CHAT,
      DiscordChannel.GG_MEMBERS_CHAT);
  private static final DiscordChannel TEST_BATPHONE_CHANNEL = DiscordChannel.TEST_BATPHONE;
  private static final DiscordChannel TEST_NONBATPHONE_CHANNEL = DiscordChannel.TEST_GENERAL;

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

  private final Config config;
  private final GameScreenshotter gameScreenshotter;
  private final Discord discord;

  // TODO: Make this a per-target rate limiter if/when you add more targets.
  private final RateLimiter rateLimiter = new RateLimiter(RATE_LIMIT);

  public RaidTargetSpawnListener(
      Config config,
      GameScreenshotter gameScreenshotter,
      Discord discord) {
    this.config = config;
    this.gameScreenshotter = gameScreenshotter;
    this.discord = discord;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    for (Map.Entry<String, String> mapEntry : TRIGGERS_AND_TARGETS.entrySet()) {
      String trigger = mapEntry.getKey();
      String target = mapEntry.getValue();
      // Use startsWith() instead of equals() to account for potential end-of-line weirdness
      // (trailing whitespace, /r, etc.).
      if (eqLogEvent.getPayload().startsWith(trigger)) {
        if (rateLimiter.getPermission()) {
          String message = String.format(BATPHONE ? BATPHONE_MESSAGE : REGULAR_MESSAGE, target);
          Optional<File> maybeScreenshot = gameScreenshotter.get();

          for (DiscordChannel discordChannel : getChannels()) {
            if (maybeScreenshot.isPresent()) {
              discord.sendMessage(discordChannel, message, maybeScreenshot.get());
            } else {
              discord.sendMessage(discordChannel, message);
            }
          }
        }
      }
    }
  }

  private List<DiscordChannel> getChannels() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return BATPHONE ? List.of(TEST_BATPHONE_CHANNEL) : List.of(TEST_NONBATPHONE_CHANNEL);
    } else {
      return BATPHONE ? PROD_BATPHONE_CHANNELS : PROD_NONBATPHONE_CHANNELS;
    }
  }

}
