// $ ./gradlew runTickContextAnalysis --args='$characterName'

package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLog;
import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TickContextAnalysis {

  private static final Duration MAXIMUM_CONTEXT_GAP = Duration.ofSeconds(15);

  public static void main(String[] args) {
    Config config = new Config();
    EqLog eqLog = new EqLog(
        Paths.get(config.getString(Property.EVERQUEST_INSTALL_DIRECTORY)),
        ZoneId.of(config.getString(Property.TIMEZONE_GAME)),
        config.getString(Property.EVERQUEST_SERVER),
        args[0],
        Instant.now().minus(Duration.ofDays(30)),
        Instant.now());
    eqLog.addListener(new TickContextAnalysisListener());
    eqLog.run();
  }

  private static class TickContextAnalysisListener implements EqLogListener {

    private static final Pattern GUILD_CHAT_PATTERN = Pattern.compile("^(.+) tells the guild, '.*");

    private TickDetector tickDetector = new TickDetector();
    private EqLogEvent lastTick = null;
    private String lastTickTaker = null;

    @Override
    public void onEvent(EqLogEvent eqLogEvent) {
      Matcher matcher = GUILD_CHAT_PATTERN.matcher(eqLogEvent.getPayload());
      if (matcher.matches()) {
        String character = matcher.group(1);

        if (character.equals(lastTickTaker)
            && lastTick != null
            && eqLogEvent.getTimestamp().isBefore(
                lastTick.getTimestamp().plus(MAXIMUM_CONTEXT_GAP))) {
          System.out.println("AFTER: " + eqLogEvent.getFullLine());

          // Bookkeeping.
          lastTickTaker = null;
        }

        if (tickDetector.containsTick(eqLogEvent)) {
          System.out.println();
          System.out.println(" TICK: " + eqLogEvent.getFullLine());

          // Bookkeeping.
          lastTick = eqLogEvent;
          lastTickTaker = character;
        }
      }
    }

  }

}
