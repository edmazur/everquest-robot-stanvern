package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TickListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.FOW_RAID_TICKS_AND_GRATSS;
  // Keep these 2 constants in sync.
  private static final Duration MAXIMUM_CONTEXT_GAP = Duration.ofSeconds(15);
  private static final String MAXIMUM_CONTEXT_GAP_DESCRIPTION = "15 seconds";

  private static final Pattern GUILD_CHAT_PATTERN = Pattern.compile("^(.+) tells the guild, '.*");

  private final Discord discord;
  private final TickDetector tickDetector;

  private Map<String, EqLogEvent> tickTakersToTicks = new HashMap<>();

  public TickListener(Discord discord, TickDetector tickDetector) {
    this.discord = discord;
    this.tickDetector = tickDetector;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    Optional<String> maybeGuildChatAuthor = getGuildChatAuthor(eqLogEvent);
    if (tickDetector.containsTick(eqLogEvent)) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "🎟️ Possible tick sighting, ET: `" + eqLogEvent.getFullLine() + "`");
      if (maybeGuildChatAuthor.isEmpty()) {
        LOGGER.log("Could not get tick taker from: " + eqLogEvent.getFullLine());
      } else {
        tickTakersToTicks.put(maybeGuildChatAuthor.get(), eqLogEvent);
      }
    } else if (maybeGuildChatAuthor.isPresent()
        && tickTakersToTicks.containsKey(maybeGuildChatAuthor.get())
        && eqLogEvent.getTimestamp().isBefore(
            tickTakersToTicks.get(maybeGuildChatAuthor.get()).getTimestamp()
                .plus(MAXIMUM_CONTEXT_GAP))) {
      discord.sendMessage(
          OUTPUT_CHANNEL,
          "⬆️ Possible tick context, ET: `" + eqLogEvent.getFullLine()
              + "` (tick-taker's next message within " + MAXIMUM_CONTEXT_GAP_DESCRIPTION + ")");
      tickTakersToTicks.remove(maybeGuildChatAuthor.get());
    }
  }

  private Optional<String> getGuildChatAuthor(EqLogEvent eqLogEvent) {
    Matcher matcher = GUILD_CHAT_PATTERN.matcher(eqLogEvent.getPayload());
    if (matcher.matches() && matcher.groupCount() == 1) {
      return Optional.of(matcher.group(1));
    } else {
      return Optional.empty();
    }
  }

}
