package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GratsDetector {

  public static final List<String> TRIGGERS = Arrays.asList("gratss", "!grats", "!gratz");

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");
  private static final Pattern SAY_CHAT_PATTERN = Pattern.compile("You say, '(.+)'");

  private Config config;

  public GratsDetector(Config config) {
    this.config = config;
  }

  public boolean containsGrats(EqLogEvent eqLogEvent) {
    Matcher matcher = getPattern().matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return false;
    }
    String chatText = matcher.group(1).toLowerCase();

    // Ignore single-word input to avoid matching input that has only the trigger and nothing else.
    if (chatText.trim().split("\\s+").length == 1) {
      return false;
    }

    return Arrays.stream(TRIGGERS.toArray(new String[0])).anyMatch(chatText::contains);
  }

  private Pattern getPattern() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return SAY_CHAT_PATTERN;
    } else {
      return GUILD_CHAT_PATTERN;
    }
  }

}
