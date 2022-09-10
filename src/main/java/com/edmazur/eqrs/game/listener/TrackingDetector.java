package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackingDetector {

  private static final Pattern GUILD_CHAT_PATTERN =
      Pattern.compile("(?:\\p{Alpha}+ tells the guild|You say to your guild), '(.+)'");
  private static final Pattern SAY_CHAT_PATTERN = Pattern.compile("You say, '(.+)'");

  private Config config;

  public TrackingDetector(Config config) {
    this.config = config;
  }

  public boolean containsTracking(EqLogEvent eqLogEvent) {
    Matcher matcher = getPattern().matcher(eqLogEvent.getPayload());
    if (!matcher.matches()) {
      return false;
    }
    String guildChatText = matcher.group(1).toLowerCase();
    return guildChatText.contains("trackbot");
  }

  private Pattern getPattern() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return SAY_CHAT_PATTERN;
    } else {
      return GUILD_CHAT_PATTERN;
    }
  }

}
