package com.edmazur.eqrs;

import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.game.RaidTarget;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class Database {

  private static final String MYSQL_CONNECTION_FORMAT_STRING = "jdbc:mysql://%s:%d/%s";
  private static final int MYSQL_PORT = 3306;

  private static final String UPDATE_TOD_SQL = "UPDATE tods SET tod = ? WHERE target = ?;";
  private static final String GET_QUAKE_SQL = "SELECT lastquake FROM quakes;";
  private static final String UPDATE_QUAKE_SQL = "UPDATE quakes SET lastquake = ?";

  private final Config config;

  public Database(Config config) {
    this.config = config;
  }

  public Optional<Integer> updateTimeOfDeath(RaidTarget raidTarget, LocalDateTime tod) {
    try {
      PreparedStatement preparedStatement = getConnection().prepareStatement(UPDATE_TOD_SQL);
      preparedStatement.setTimestamp(1, Timestamp.valueOf(tod));
      preparedStatement.setString(2, raidTarget.getName());
      return Optional.of(preparedStatement.executeUpdate());
    } catch (SQLException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<LocalDateTime> getQuakeTime() {
    try {
      ResultSet resultSet = getConnection().createStatement().executeQuery(GET_QUAKE_SQL);
      if (resultSet.next()) {
        return Optional.of(resultSet.getTimestamp(1).toLocalDateTime());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public Optional<Integer> updateQuakeTime(LocalDateTime quakeTime) {
    try {
      PreparedStatement preparedStatement = getConnection().prepareStatement(UPDATE_QUAKE_SQL);
      preparedStatement.setTimestamp(1, Timestamp.valueOf(quakeTime));
      return Optional.of(preparedStatement.executeUpdate());
    } catch (SQLException e) {
      e.printStackTrace();
      return Optional.empty();
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
