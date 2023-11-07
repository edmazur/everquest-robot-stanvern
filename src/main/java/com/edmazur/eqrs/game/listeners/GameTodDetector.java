package com.edmazur.eqrs.game.listeners;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameTodDetector {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");
  private static final Pattern SAY_CHAT_PATTERN = Pattern.compile("You say, '(.+)'");

  private Config config;

  public GameTodDetector() { }

  public Optional<String> getTodMessage(EqLogEvent eqLogEvent) {
    Matcher matcher = getPattern().matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return Optional.empty();
    }
    String chatText = matcher.group(1);
    String chatTextLower = chatText.toLowerCase();
    if (chatTextLower.contains("tod") && !chatTextLower.contains("today")) {
      return Optional.of(chatText);
    } else {
      return Optional.empty();
    }
  }

  private Pattern getPattern() {
    if (Config.getConfig().isDebug()) {
      return SAY_CHAT_PATTERN;
    } else {
      return GUILD_CHAT_PATTERN;
    }
  }

}
