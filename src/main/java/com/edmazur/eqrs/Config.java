package com.edmazur.eqrs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// TODO: Is there a way to force property X to always be read as type Y?
public class Config {

  private static final String CONFIG_FILE_NAME = "app.config";

  public enum Property {

    // Either:
    // -  "active": For game client that's actively being played. This can be actually playing or
    //              just AFK tracking (probably Yael).
    // - "passive": For game client that's passively being played. This means the client is sitting
    //              in one spot (probably EC tunnel) and never moving or taking any action at all.
    //              It's there pretty much just for /gu and earthquake messages.
    BASE_MODE("base.mode"),

    DISCORD_PRIVATE_KEY("discord.private_key"),

    // When enabled:
    // - Discord input will only be accepted from me via DM.
    // - Discord output will only go to me as a DM (i.e. no channel output).
    // - Database writes will be skipped and SQL will instead just be logged.
    DEBUG("debug"),

    EVERQUEST_INSTALL_DIRECTORY("everquest.install_directory"),
    EVERQUEST_RAID_TARGETS_ENDPOINT("everquest.raid_targets_endpoint"),
    EVERQUEST_SERVER("everquest.server"),

    MYSQL_DATABASE_PROD("mysql.database.prod"),
    MYSQL_DATABASE_TEST("mysql.database.test"),
    MYSQL_HOST_PROD("mysql.host.prod"),
    MYSQL_HOST_TEST("mysql.host.test"),
    MYSQL_PASSWORD_PROD("mysql.password.prod"),
    MYSQL_PASSWORD_TEST("mysql.password.test"),
    MYSQL_USERNAME_PROD("mysql.username.prod"),
    MYSQL_USERNAME_TEST("mysql.username.test"),

    // TODO: Use these everywhere instead of ever relying on machine timezone.
    // The timezone that game logs are in.
    TIMEZONE_GAME("timezone.game"),
    // The timezone that the guild uses for scheduling.
    TIMEZONE_GUILD("timezone.guild")

    ;

    private final String name;

    private Property(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  private Properties properties;

  public Config() {
    properties = new Properties();
    try {
      properties.load(new FileInputStream(CONFIG_FILE_NAME));
    } catch (IOException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
    }
  }

  // Only the debug property supports being set externally like this in order to facilitate setting
  // it via the command line.
  // TODO: Look into how to eliminate this by supporting different config files for
  // development/production environments.
  public void enableDebug() {
    properties.setProperty(Property.DEBUG.getName(), "true");
  }

  public boolean getBoolean(Property property) {
    return Boolean.parseBoolean(properties.getProperty(property.getName()));
  }

  public String getString(Property property) {
    return properties.getProperty(property.getName());
  }

}
