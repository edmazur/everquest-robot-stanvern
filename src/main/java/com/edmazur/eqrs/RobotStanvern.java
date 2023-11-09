package com.edmazur.eqrs;

import com.edmazur.eqlp.EqLog;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.listener.AuditListener;
import com.edmazur.eqrs.discord.listener.CharInfoScreenshotListener;
import com.edmazur.eqrs.discord.listener.DiscordParkListener;
import com.edmazur.eqrs.discord.listener.DiscordTodListener;
import com.edmazur.eqrs.discord.listener.GratsChannelListener;
import com.edmazur.eqrs.discord.listener.ItemListener;
import com.edmazur.eqrs.discord.listener.LootStatusListener;
import com.edmazur.eqrs.discord.listener.LootStatusRequester;
import com.edmazur.eqrs.discord.speaker.TodWindowSpeaker;
import com.edmazur.eqrs.game.listener.active.FteListener;
import com.edmazur.eqrs.game.listener.active.RaidTargetSpawnListener;
import com.edmazur.eqrs.game.listener.passive.EarthquakeListener;
import com.edmazur.eqrs.game.listener.passive.GameTodListener;
import com.edmazur.eqrs.game.listener.passive.GratsListener;
import com.edmazur.eqrs.game.listener.passive.HeartbeatListener;
import com.edmazur.eqrs.game.listener.passive.MotdListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotStanvern {

  private static final Logger LOGGER = new Logger();

  private enum Mode {
    ACTIVE,
    PASSIVE,
  }

  public static void main(String[] args) {
    // Parse command line arguments.
    // TODO: Use a proper library for this if it grows more complex.
    if (args.length == 0) {
      LOGGER.log("Usage: CharacterName [--debug]");
      System.exit(-1);
    }
    // TODO: Automatically switch between logs as you change characters.
    // This is final to avoid a VariableDeclarationUsageDistance checkstyle warning.
    // TODO: Disable that check?
    final String character = args[0];
    boolean enableDebug = args.length == 2 && args[1].equals("--debug");

    if (enableDebug) {
      Config.getConfig().enableDebug();
    }

    if (Config.getConfig().isDebug()) {
      LOGGER.log("Debug mode enabled, Discord output will only be sent as DM and database writes "
          + "will be skipped (SQL will be logged)");
    }

    String suppliedMode = Config.getConfig().getString(Config.Property.BASE_MODE);
    Mode mode = null;
    try {
      if (suppliedMode.isBlank()) {
        LOGGER.log(Config.Property.BASE_MODE.getName() + " is a required config field.");
        System.exit(-1);
      }
      mode = Mode.valueOf(suppliedMode.toUpperCase());
    } catch (IllegalArgumentException e) {
      LOGGER.log("Unable to parse mode from: " + suppliedMode);
      System.exit(-1);
    }

    // Uncomment to send one-off images/messages.
    // TODO: Add a better way to send one-off images/messages that doesn't require code changes.
    /*
    File image = new File("/home/mazur/eclipse-workspace/RobotStanvern/img/angry-robot.gif");
    if (image.exists()) {
      Discord.getDiscord().sendMessage(DiscordChannel.TOD, image);
    }
    */

    List<EqLogListener> eqLogListeners = new ArrayList<>();

    if (mode == Mode.PASSIVE) {
      // Set up discord listeners
      new DiscordTodListener();
      new AuditListener();
      new CharInfoScreenshotListener();
      new ItemListener();
      new GratsChannelListener();
      new LootStatusListener();
      new DiscordParkListener();

      // Add loot status requester.
      ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Config.getConfig()
          .getString(Property.TIMEZONE_GUILD)));
      // Run at 5am daily.
      ZonedDateTime nextRun = now.withHour(5).withMinute(0).withSecond(0);
      if (now.isAfter(nextRun)) {
        nextRun = nextRun.plusDays(1);
      }
      ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
      scheduler.scheduleAtFixedRate(
          new LootStatusRequester(),
          Duration.between(now, nextRun).getSeconds(),
          TimeUnit.DAYS.toSeconds(1),
          TimeUnit.SECONDS);

      // Add heartbeat listener.
      HeartbeatListener heartbeatListener = new HeartbeatListener();
      ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
      scheduledExecutorService.scheduleAtFixedRate(heartbeatListener, 1, 1, TimeUnit.SECONDS);
      eqLogListeners.add(heartbeatListener);

      // Add MotD listener.
      MotdListener motdListener = new MotdListener();
      eqLogListeners.add(motdListener);

      // Add ToD listener.
      GameTodListener gameTodListener = new GameTodListener();
      eqLogListeners.add(gameTodListener);

      // Add grats listener.
      GratsListener gratsListener = new GratsListener();
      eqLogListeners.add(gratsListener);

      // Add earthquake listener.
      EarthquakeListener earthquakeListener = new EarthquakeListener();
      eqLogListeners.add(earthquakeListener);

      // Add ToD window speaker.
      scheduledExecutorService.scheduleAtFixedRate(
          new TodWindowSpeaker(), 0, 1, TimeUnit.MINUTES);
    }

    if (mode == Mode.ACTIVE) {
      // Add FTE listener.
      eqLogListeners.add(new FteListener());

      // Add raid target spawn listener.
      RaidTargetSpawnListener raidTargetSpawnListener = new RaidTargetSpawnListener();
      eqLogListeners.add(raidTargetSpawnListener);
    }

    // Parse the log.
    while (true) {
      EqLog eqLog = new EqLog(
          Paths.get(Config.getConfig().getString(Property.EVERQUEST_INSTALL_DIRECTORY)),
          ZoneId.of(Config.getConfig().getString(Property.TIMEZONE_GAME)),
          Config.getConfig().getString(Property.EVERQUEST_SERVER),
          character,
          Instant.now(),
          Instant.MAX);
      LOGGER.log("Reading log from: " + character);
      for (EqLogListener eqLogListener : eqLogListeners) {
        eqLog.addListener(eqLogListener);
      }
      try {
        eqLog.run();
      } catch (Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();
        LOGGER.log(
            "Uncaught exception from main thread, restarting. This generally should not happen.\n"
            + stackTrace);
      }
    }
  }

}
