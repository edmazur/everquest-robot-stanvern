package com.edmazur.eqrs;

import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.game.RaidTarget;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Database {

  private static final Logger LOGGER = new Logger();

  private static final String MYSQL_CONNECTION_FORMAT_STRING = "jdbc:mysql://%s:%d/%s";
  private static final int MYSQL_PORT = 3306;

  private static final String UPDATE_TOD_SQL = "UPDATE tods SET tod = '%s' WHERE target = '%s';";
  private static final String GET_QUAKE_SQL = "SELECT lastquake FROM quakes;";
  private static final String UPDATE_QUAKE_SQL = "UPDATE quakes SET lastquake = '%s'";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final Config config;

  public Database(Config config) {
    this.config = config;
  }

  public void updateTimeOfDeath(RaidTarget raidTarget, LocalDateTime tod) {
    String query = String.format(
        UPDATE_TOD_SQL,
        DATE_TIME_FORMATTER.format(tod),
        raidTarget.getName());
    update(query);
  }

  public LocalDateTime getQuakeTime() {
    try {
      ResultSet resultSet = getConnection().createStatement().executeQuery(GET_QUAKE_SQL);
      if (resultSet.next()) {
        return resultSet.getTimestamp(1).toLocalDateTime();
      } else {
        LOGGER.log("Tried to get quake time, but no rows returned from database.");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // TODO: Handle this more gracefully.
    throw new RuntimeException("Unable to get quake time");
  }

  public void updateQuakeTime(LocalDateTime quakeTime) {
    String query = String.format(UPDATE_QUAKE_SQL, DATE_TIME_FORMATTER.format(quakeTime));
    update(query);
  }

  private void update(String query) {
    try {
      getConnection().createStatement().executeUpdate(query);
    } catch (SQLException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private Connection getConnection() {
    boolean debug = config.getBoolean(Property.DEBUG);

    String host = config.getString(
        debug ? Config.Property.MYSQL_HOST_TEST : Config.Property.MYSQL_HOST_PROD);
    String database = config.getString(
        debug ? Config.Property.MYSQL_DATABASE_TEST : Config.Property.MYSQL_DATABASE_PROD);
    String username = config.getString(
        debug ? Config.Property.MYSQL_USERNAME_TEST : Config.Property.MYSQL_USERNAME_PROD);
    String password = config.getString(
        debug ? Config.Property.MYSQL_PASSWORD_TEST : Config.Property.MYSQL_PASSWORD_PROD);

    String connectionString =
        String.format(MYSQL_CONNECTION_FORMAT_STRING, host, MYSQL_PORT, database);
    try {
      return DriverManager.getConnection(connectionString, username, password);
    } catch (SQLException e) {
      // TODO: Handle database unavailability more gracefully.
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

}
