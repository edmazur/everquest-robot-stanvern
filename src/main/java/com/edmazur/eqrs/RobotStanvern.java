package com.edmazur.eqrs;

import com.edmazur.eqlp.EqLog;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordTableFormatter;
import com.edmazur.eqrs.discord.listener.AnnouncementListener;
import com.edmazur.eqrs.discord.listener.BatphoneListener;
import com.edmazur.eqrs.discord.listener.CharInfoScreenshotListener;
import com.edmazur.eqrs.discord.listener.DiscordTodListener;
import com.edmazur.eqrs.discord.listener.ItemListener;
import com.edmazur.eqrs.discord.speaker.TodWindowSpeaker;
import com.edmazur.eqrs.game.CharInfoOcrScrapeComparator;
import com.edmazur.eqrs.game.CharInfoScraper;
import com.edmazur.eqrs.game.ExpPercentToNextLevelScraper;
import com.edmazur.eqrs.game.GameScreenshotter;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import com.edmazur.eqrs.game.RaidTargetTableMaker;
import com.edmazur.eqrs.game.RaidTargets;
import com.edmazur.eqrs.game.listener.DiceDetector;
import com.edmazur.eqrs.game.listener.DiceListener;
import com.edmazur.eqrs.game.listener.EarthquakeDetector;
import com.edmazur.eqrs.game.listener.EarthquakeListener;
import com.edmazur.eqrs.game.listener.FteListener;
import com.edmazur.eqrs.game.listener.GameTodDetector;
import com.edmazur.eqrs.game.listener.GameTodListener;
import com.edmazur.eqrs.game.listener.GameTodParser;
import com.edmazur.eqrs.game.listener.GratsDetector;
import com.edmazur.eqrs.game.listener.GratsListener;
import com.edmazur.eqrs.game.listener.HeartbeatListener;
import com.edmazur.eqrs.game.listener.MotdListener;
import com.edmazur.eqrs.game.listener.RaidTargetSpawnListener;
import com.edmazur.eqrs.game.listener.TickDetector;
import com.edmazur.eqrs.game.listener.TickListener;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotStanvern {

  private static final Logger LOGGER = new Logger();

  public static void main(String[] args) {
    // Parse command line arguments.
    // TODO: Use a proper library for this if it grows more complex.
    if (args.length == 0) {
      LOGGER.log("Usage: CharacterName [--debug]");
    }
    // TODO: Automatically switch between logs as you change characters.
    // This is final to avoid a VariableDeclarationUsageDistance checkstyle warning.
    // TODO: Disable that check?
    final String character = args[0];
    boolean enableDebug = args.length == 2 && args[1].equals("--debug");

    Config config = new Config();
    if (enableDebug) {
      config.enableDebug();
    }

    if (config.getBoolean(Property.DEBUG)) {
      LOGGER.log("Debug mode enabled, Discord output will only be sent as DM and database writes "
          + "will be skipped (SQL will be logged)");
    }

    Discord discord = new Discord(config);

    // Uncomment to send one-off images/messages.
    // TODO: Add a better way to send one-off images/messages that doesn't require code changes.
    /*
    File image = new File("/home/mazur/eclipse-workspace/RobotStanvern/img/angry-robot.gif");
    if (image.exists()) {
      discord.sendMessage(DiscordChannel.TOD, image);
    }
    */

    Database database = new Database(config);
    Json json = new Json();
    RaidTargets raidTargets = new RaidTargets(config, json);
    Pager pager = new Pager(config);
    SoundPlayer soundPlayer = new SoundPlayer();
    // TODO: Set this up more like how game log messages are received centrally and passed out to
    // listeners?
    new DiscordTodListener(config, discord, database, raidTargets);
    new AnnouncementListener(config, discord);
    new BatphoneListener(config, discord, pager, soundPlayer);
    Ocr ocr = new Ocr();
    CharInfoOcrScrapeComparator charInfoOcrScrapeComparator = new CharInfoOcrScrapeComparator();
    ExpPercentToNextLevelScraper expPercentToNextLevelScraper = new ExpPercentToNextLevelScraper();
    CharInfoScraper charInfoScraper =
        new CharInfoScraper(ocr, charInfoOcrScrapeComparator, expPercentToNextLevelScraper);
    new CharInfoScreenshotListener(config, discord, charInfoScraper).init();
    ItemDatabase itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
    ItemScreenshotter itemScreenshotter = new ItemScreenshotter();
    new ItemListener(config, discord, itemDatabase, itemScreenshotter);

    List<EqLogListener> eqLogListeners = new ArrayList<>();

    // Add FTE listener.
    eqLogListeners.add(new FteListener(discord));

    // Add heartbeat listener.
    HeartbeatListener heartbeatListener = new HeartbeatListener(discord);
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    scheduledExecutorService.scheduleAtFixedRate(heartbeatListener, 1, 1, TimeUnit.SECONDS);
    eqLogListeners.add(heartbeatListener);

    // Add raid target spawn listener.
    GameScreenshotter gameScreenshotter = new GameScreenshotter();
    RaidTargetSpawnListener raidTargetSpawnListener =
        new RaidTargetSpawnListener(gameScreenshotter, discord);
    eqLogListeners.add(raidTargetSpawnListener);

    // Add MotD listener.
    MotdListener motdListener = new MotdListener(discord);
    eqLogListeners.add(motdListener);

    // Add ToD listener.
    GameTodDetector gameTodDetector = new GameTodDetector();
    GameTodParser gameTodParser = new GameTodParser(raidTargets);
    GameTodListener gameTodListener = new GameTodListener(discord, gameTodDetector, gameTodParser);
    eqLogListeners.add(gameTodListener);

    // Add tick listener.
    TickDetector tickDetector = new TickDetector();
    TickListener tickListener = new TickListener(discord, tickDetector);
    eqLogListeners.add(tickListener);

    // Add grats listener.
    GratsDetector gratsDetector = new GratsDetector();
    GratsListener gratsListener =
        new GratsListener(discord, gratsDetector, itemDatabase, itemScreenshotter);
    eqLogListeners.add(gratsListener);

    // Add dice listener.
    DiceDetector diceDetector = new DiceDetector();
    DiceListener diceListener = new DiceListener(diceDetector, soundPlayer);
    eqLogListeners.add(diceListener);

    // Add earthquake listener.
    EarthquakeDetector earthquakeDetector = new EarthquakeDetector();
    EarthquakeListener earthquakeListener = new EarthquakeListener(discord, earthquakeDetector);
    eqLogListeners.add(earthquakeListener);

    // Add ToD window speaker.
    RaidTargetTableMaker raidTargetTableMaker = new RaidTargetTableMaker(config, raidTargets);
    DiscordTableFormatter discordTableFormatter = new DiscordTableFormatter();
    TodWindowSpeaker todWindowSpeaker =
        new TodWindowSpeaker(config, discord, raidTargetTableMaker, discordTableFormatter);
    scheduledExecutorService.scheduleAtFixedRate(todWindowSpeaker, 0, 1, TimeUnit.MINUTES);

    // Parse the log.
    EqLog eqLog = new EqLog(
        Paths.get(config.getString(Property.EVERQUEST_INSTALL_DIRECTORY)),
        ZoneId.of(config.getString(Property.TIMEZONE_GAME)),
        config.getString(Property.EVERQUEST_SERVER),
        character,
        Instant.now(),
        Instant.MAX);
    LOGGER.log("Reading log from: " + character);
    for (EqLogListener eqLogListener : eqLogListeners) {
      eqLog.addListener(eqLogListener);
    }
    eqLog.run();
  }

}
