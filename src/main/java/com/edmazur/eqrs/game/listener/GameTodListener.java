package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;

public class GameTodListener implements EqLogListener {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("M/d HH:mm:ss");

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.FOW_TOD;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_TOD;

  private final Config config;
  private final Discord discord;
  private final GameTodDetector gameTodDetector;
  private final GameTodParser gameTodParser;

  public GameTodListener(
      Config config,
      Discord discord,
      GameTodDetector gameTodDetector,
      GameTodParser gameTodParser) {
    this.config = config;
    this.discord = discord;
    this.gameTodDetector = gameTodDetector;
    this.gameTodParser = gameTodParser;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gameTodDetector.containsTod(eqLogEvent)) {
      CompletableFuture<Message> messageFuture = discord.sendMessage(
          getChannel(), "⏲ Possible ToD sighting, ET: `" + eqLogEvent.getFullLine() + "`");
      Optional<GameTodParseResult> maybeGameTodParseResult = gameTodParser.parse(eqLogEvent);
      if (maybeGameTodParseResult.isPresent()) {
        messageFuture.join().reply(getTodInput(maybeGameTodParseResult.get()));
      }
    }
  }

  private String getTodInput(GameTodParseResult gameTodParseResult) {
    return String.format("!tod %s, %s",
        gameTodParseResult.getRaidTarget().getName(),
        DATE_TIME_FORMATTER.format(gameTodParseResult.getTimeOfDeath()));
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
