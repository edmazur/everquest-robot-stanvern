package com.edmazur.eqrs;

import com.edmazur.eqlp.EqLog;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.listener.LootStatusRequester;
import com.edmazur.eqrs.discord.speaker.SubscriptionSpeaker;
import com.edmazur.eqrs.discord.speaker.TodWindowSpeaker;
import com.edmazur.eqrs.game.listeners.passive.HeartbeatListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.javacord.api.listener.message.MessageCreateListener;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

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

    if (mode == Mode.PASSIVE) {
      initializeDiscordListeners();

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

      /* SCHEDULED TASKS */

      // Set up the Scheduled Task Executor
      ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

      // Add heartbeat listener
      scheduledExecutorService.scheduleAtFixedRate(
          new HeartbeatListener(), 1, 1, TimeUnit.SECONDS);
      // Add ToD window speaker.
      scheduledExecutorService.scheduleAtFixedRate(
          new TodWindowSpeaker(), 0, 1, TimeUnit.MINUTES);
      // Add Window Subscription speaker
      scheduledExecutorService.scheduleAtFixedRate(
          new SubscriptionSpeaker(), 0, 1, TimeUnit.MINUTES);
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
      initializeGameListeners(eqLog, mode);
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

  private static void initializeDiscordListeners() {
    // Discover all Discord listeners
    Reflections reflections = new Reflections(
        "com.edmazur.eqrs.discord.listener", Scanners.SubTypes);
    Set<Class<? extends MessageCreateListener>> listeners = reflections.getSubTypesOf(
        MessageCreateListener.class);
    for (Class<? extends MessageCreateListener> listenerClass : listeners) {
      MessageCreateListener listener = null;
      try {
        listener = listenerClass.getDeclaredConstructor().newInstance();
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
               | InvocationTargetException ignored) {
        LOGGER.log("Error creating Discord listener: " + listenerClass.getName());
      }
      Discord.getDiscord().addListener(listener);
      //LOGGER.log("Added Discord listener: " + listenerClass.getName());
    }
  }

  private static void initializeGameListeners(EqLog eqLog, Mode mode) {
    String packagePath;
    if (mode == Mode.PASSIVE) {
      packagePath = "com.edmazur.eqrs.game.listeners.passive";
    } else if (mode == Mode.ACTIVE) {
      packagePath = "com.edmazur.eqrs.game.listeners.active";
    } else {
      return;
    }

    // Discover all EqLogListeners in the specified package
    Reflections reflections = new Reflections(packagePath, Scanners.SubTypes);
    Set<Class<? extends EqLogListener>> listeners = reflections.getSubTypesOf(
        EqLogListener.class);
    for (Class<? extends EqLogListener> listenerClass : listeners) {
      EqLogListener listener = null;
      try {
        listener = listenerClass.getDeclaredConstructor().newInstance();
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
               | InvocationTargetException ignored) {
        LOGGER.log("Error creating listener: " + listenerClass.getName());
      }
      eqLog.addListener(listener);
      //LOGGER.log("Added EqLog listener: " + listenerClass.getName());
    }
  }

}
