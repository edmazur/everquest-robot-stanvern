package com.edmazur.eqrs.game.listeners.passive;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.listeners.GameTodDetector;
import com.edmazur.eqrs.game.listeners.GameTodParseResult;
import com.edmazur.eqrs.game.listeners.GameTodParser;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;

public class GameTodListener implements EqLogListener {

  private static final Logger LOGGER = new Logger();
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("M/d HH:mm:ss");

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TOD;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_TOD;
  private final GameTodDetector gameTodDetector;
  private final GameTodParser gameTodParser;

  public GameTodListener() {
    this.gameTodDetector = new GameTodDetector();
    this.gameTodParser = new GameTodParser();
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    Optional<String> maybeTodMessage = gameTodDetector.getTodMessage(eqLogEvent);
    if (maybeTodMessage.isPresent()) {
      GameTodParseResult gameTodParseResult =
          gameTodParser.parse(eqLogEvent, maybeTodMessage.get());
      String discordMessage = "⏲ Possible ToD sighting, ET: `" + eqLogEvent.getFullLine() + "`";
      if (!gameTodParseResult.wasSuccessfullyParsed()) {
        discordMessage +=
            " (**not** auto-parsing, reason: " + gameTodParseResult.getError() + ")";
      }
      CompletableFuture<Message> messageFuture =
          Discord.getDiscord().sendMessage(getChannel(), discordMessage);
      if (gameTodParseResult.wasSuccessfullyParsed()) {
        messageFuture.join().reply(getTodInput(gameTodParseResult));
      }
    }
  }

  private String getTodInput(GameTodParseResult gameTodParseResult) {
    return String.format("!tod %s, %s",
        gameTodParseResult.getRaidTarget().getName(),
        DATE_TIME_FORMATTER.format(gameTodParseResult.getTimeOfDeath()));
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().isDebug()) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
