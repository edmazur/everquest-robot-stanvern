package com.edmazur.eqrs.game.listeners.active;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.RateLimiter;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.GameScreenshotter;
import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class RaidTargetSpawnListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();
  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_RAID_DISCUSSION;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private static final String MESSAGE = "@everyone %s POP! ET: `%s`";

  private static Map<String, String> TRIGGERS_AND_TARGETS = Map.ofEntries(
      Map.entry("Master Yael begins to cast a spell.", "YAEL"),
      // The space before "Tunare" is *not* a typo:
      // [Tue May 02 21:56:23 2023]  Tunare begins to cast a spell.
      // Best guess is that this has to do with the "fake" tree Tunare vs. "real" field Tunare.
      Map.entry(" Tunare begins to cast a spell.", "TUNARE"));

  private static final Duration RATE_LIMIT = Duration.ofHours(24);

  private final GameScreenshotter gameScreenshotter;

  // TODO: Make this a per-target rate limiter if/when you add more targets.
  private final RateLimiter rateLimiter = new RateLimiter(RATE_LIMIT);

  public RaidTargetSpawnListener() {
    this.gameScreenshotter = new GameScreenshotter();
    LOGGER.log("%s running", this.getClass().getName());
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
          String message = String.format(MESSAGE, target, eqLogEvent.getFullLine());
          Discord.getDiscord().sendMessage(getChannel(), message);

          Optional<File> maybeScreenshot = gameScreenshotter.get();
          maybeScreenshot.ifPresent(file -> Discord.getDiscord().sendMessage(getChannel(), file));
        }
      }
    }
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().isDebug()) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
