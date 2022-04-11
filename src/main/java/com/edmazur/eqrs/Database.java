package com.edmazur.eqrs;

import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.game.RaidTarget;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {

  private static final Logger LOGGER = new Logger();

  private static final String MYSQL_CONNECTION_FORMAT_STRING = "jdbc:mysql://%s:%d/%s";
  private static final int MYSQL_PORT = 3306;

  private static final String SELECT_TARGETS_SQL = "SELECT target, aliases FROM targets;";
  private static final String UPDATE_TOD_SQL = "UPDATE tods SET tod = '%s' WHERE target = '%s';";
  private static final String UPDATE_QUAKE_SQL = "UPDATE quakes SET lastquake = '%s'";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final Config config;

  public Database(Config config) {
    this.config = config;
  }

  public List<RaidTarget> getAllTargets() {
    try {
      List<RaidTarget> raidTargets = new ArrayList<>();
      ResultSet resultSet = getConnection().createStatement().executeQuery(SELECT_TARGETS_SQL);
      while (resultSet.next()) {
        String name = resultSet.getString("target");
        String[] aliases = resultSet.getString("aliases").split(",");
        raidTargets.add(new RaidTarget(name, aliases));
      }
      return raidTargets;
    } catch (SQLException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

  public void updateTimeOfDeath(RaidTarget raidTarget, LocalDateTime tod) {
    String query = String.format(
        UPDATE_TOD_SQL,
        DATE_TIME_FORMATTER.format(tod),
        raidTarget.getName());
    update(query);
  }

  public void updateQuakeTime(LocalDateTime quakeTime) {
    String query = String.format(UPDATE_QUAKE_SQL, DATE_TIME_FORMATTER.format(quakeTime));
    update(query);
  }

  private void update(String query) {
    if (config.getBoolean(Property.DEBUG)) {
      LOGGER.log("Debug mode enabled, skipping query: " + query);
      return;
    }

    try {
      getConnection().createStatement().executeUpdate(query);
    } catch (SQLException e) {
      // TODO: Handle this more gracefully.
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private Connection getConnection() {
    String host = config.getString(Config.Property.MYSQL_HOST);
    String database = config.getString(Config.Property.MYSQL_DATABASE);
    String username = config.getString(Config.Property.MYSQL_USERNAME);
    String password = config.getString(Config.Property.MYSQL_PASSWORD);

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
