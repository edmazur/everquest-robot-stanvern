package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;

public class GameTodListener implements EqLogListener {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("M/d HH:mm:ss");

  private static final DiscordChannel OUTPUT_CHANNEL = DiscordChannel.FOW_TOD;

  private final Discord discord;
  private final GameTodDetector gameTodDetector;
  private final GameTodParser gameTodParser;

  public GameTodListener(
      Discord discord,
      GameTodDetector gameTodDetector,
      GameTodParser gameTodParser) {
    this.discord = discord;
    this.gameTodDetector = gameTodDetector;
    this.gameTodParser = gameTodParser;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gameTodDetector.containsTod(eqLogEvent)) {
      CompletableFuture<Message> messageFuture = discord.sendMessage(
          OUTPUT_CHANNEL,
          "⏲ Possible ToD sighting, ET: `" + eqLogEvent.getFullLine() + "`");
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

}
