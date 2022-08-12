package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MotdListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();

  private static final Pattern GAME_MOTD_PATTERN = Pattern.compile("GUILD MOTD: .+ - .+");
  private static final Pattern DISCORD_MOTD_PATTERN =
      Pattern.compile("`" + GAME_MOTD_PATTERN.pattern() + "`");

  private static final List<DiscordChannel> PROD_CHANNELS = List.of(
      DiscordChannel.FOW_RAIDER_GMOTD,
      DiscordChannel.TBD_GMOTD);
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;

  public MotdListener(Config config, Discord discord) {
    this.config = config;
    this.discord = discord;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    Matcher matcher = GAME_MOTD_PATTERN.matcher(eqLogEvent.getPayload());
    if (matcher.matches()) {
      for (DiscordChannel discordChannel : getChannels()) {
        // Avoid repeating the same MotD when you manually /get or login.
        Optional<String> maybeCurrentMotd = getCurrentMotd(discordChannel);
        if (maybeCurrentMotd.isEmpty()) {
          LOGGER.log("Could not read current MotD from Discord. This should not happen.");
        } else {
          if (maybeCurrentMotd.get().equals(eqLogEvent.getPayload())) {
            continue;
          }
        }

        // TODO: Maybe avoid sending multiple MotDs in quick succession (e.g. from fixing typos) by
        // waiting a bit and only sending latest MotD.
        discord.sendMessage(discordChannel, "`" + eqLogEvent.getPayload() + "`");
      }
    }
  }

  /**
   * Gets the current MotD as reported in the Discord channel.
   */
  public Optional<String> getCurrentMotd(DiscordChannel discordChannel) {
    Optional<String> maybeMotd = discord.getLastMessageMatchingPredicate(
        discordChannel,
        DiscordPredicate.isFromYourself().and(
            DiscordPredicate.textMatchesPattern(DISCORD_MOTD_PATTERN)));
    if (maybeMotd.isEmpty()) {
      return maybeMotd;
    } else {
      String motd = maybeMotd.get();
      return Optional.of(motd.substring(1, motd.length() - 1));
    }
  }

  private List<DiscordChannel> getChannels() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return List.of(TEST_CHANNEL);
    } else {
      return PROD_CHANNELS;
    }
  }

}
