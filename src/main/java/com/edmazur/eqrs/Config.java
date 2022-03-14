package com.edmazur.eqrs;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

  private static final String CONFIG_FILE_NAME = "app.config";

  public enum Property {

    DISCORD_PRIVATE_KEY("discord.private_key"),

    MYSQL_DATABASE("mysql.database"),
    MYSQL_HOST("mysql.host"),
    MYSQL_PASSWORD("mysql.password"),
    MYSQL_USERNAME("mysql.username"),

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

  public String getString(Property property) {
    return properties.getProperty(property.getName());
  }

}