package com.edmazur.eqrs;

import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.game.ParkLocation;
import com.edmazur.eqrs.game.ParkLocations;
import com.edmazur.eqrs.game.RaidTarget;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Database {

  private static final Logger LOGGER = new Logger();
  private static final String MYSQL_CONNECTION_FORMAT_STRING = "jdbc:mysql://%s:%d/%s";
  private static final int MYSQL_PORT = 3306;

  private static Database database;

  // CHECKSTYLE.OFF: OperatorWrap

  // ToD SQL.
  private static final String UPDATE_TOD_SQL =
      "UPDATE tods " +
      "SET tod = ? " +
      "WHERE target = ?;";
  private static final String GET_QUAKE_SQL =
      "SELECT lastquake " +
      "FROM quakes;";
  private static final String UPDATE_QUAKE_SQL =
      "UPDATE quakes " +
      "SET lastquake = ?;";

  // Park SQL.
  private static final String GET_PARK_LOCATIONS_SQL =
      "SELECT id, name, aliases " +
      "FROM park_locations;";
  private static final String UPDATE_BOT_LOCATION_SQL =
      "INSERT INTO bots (name, location, last_updated) " +
      "VALUES(?, ?, NOW()) " +
      "ON DUPLICATE KEY UPDATE location = ?, last_updated = NOW();";

  // Subscription SQL.
  private static final String GET_SUBSCRIPTIONS_BY_USER_SQL =
      "SELECT target, expiry " +
      "FROM subscriptions " +
      "WHERE user_id = ?;";

  private static final String GET_SUBSCRIPTIONS_SQL =
      "SELECT target, expiry, user_id " +
      "FROM subscriptions;";

  private static final String GET_SUBSCRIPTIONS_FOR_NOTIFICATION_SQL =
      "SELECT target, expiry, user_id " +
      "FROM subscriptions " +
      "WHERE last_notified < DATE_SUB(NOW(), INTERVAL 31 MINUTE);";

  private static final String ADD_SUBSCRIPTION_SQL =
      "INSERT INTO subscriptions (user_id, target, expiry, last_notified) " +
      "VALUES(?, ?, DATE_ADD(NOW(), INTERVAL 30 DAY), 0);";

  private static final String REMOVE_SUBSCRIPTION_SQL =
      "DELETE FROM subscriptions " +
      "WHERE user_id = ? AND target = ?;";

  private static final String MARK_SUBSCRIPTION_NOTIFIED_SQL =
      "UPDATE subscriptions " +
      "SET last_notified = NOW() " +
      "WHERE user_id = ? AND target = ?;";

  private static final String CLEAN_EXPIRED_SUBSCRIPTIONS_SQL =
      "DELETE FROM subscriptions " +
      "WHERE expiry <= NOW();";

  // CHECKSTYLE.ON: OperatorWrap

  private Database() { }

  public static Database getDatabase() {
    if (database == null) {
      database = new Database();
    }
    return database;
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

  public Optional<ParkLocations> getParkLocations() {
    List<ParkLocation> parkLocations = Lists.newArrayList();
    try {
      ResultSet resultSet = getConnection().createStatement().executeQuery(GET_PARK_LOCATIONS_SQL);
      while (resultSet.next()) {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        List<String> aliases = Arrays.asList(resultSet.getString("aliases").split(","));
        parkLocations.add(new ParkLocation(id, name, aliases));
      }
      return Optional.of(new ParkLocations(parkLocations));
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public boolean updateBotLocation(String name, ParkLocation parkLocation) {
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(UPDATE_BOT_LOCATION_SQL);
      preparedStatement.setString(1, name);
      preparedStatement.setInt(2, parkLocation.getId());
      preparedStatement.setInt(3, parkLocation.getId());
      preparedStatement.execute();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public List<Subscription> getSubscriptionsForUser(long userId) {
    LOGGER.log("Getting subscriptions for " + userId);
    List<Subscription> subscriptionList = new ArrayList<>();
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(GET_SUBSCRIPTIONS_BY_USER_SQL);
      preparedStatement.setLong(1, userId);
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        String targetName = resultSet.getString("target");
        Timestamp expiryTime = resultSet.getTimestamp("expiry");
        subscriptionList.add(new Subscription(targetName, userId, expiryTime));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return subscriptionList;
  }

  public List<Subscription> getSubscriptionsForNotification() {
    //LOGGER.log("Getting all subscriptions with no recent notifications.");
    List<Subscription> subscriptionList = new ArrayList<>();
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(GET_SUBSCRIPTIONS_FOR_NOTIFICATION_SQL);
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        String targetName = resultSet.getString("target");
        Timestamp expiryTime = resultSet.getTimestamp("expiry");
        long userId = resultSet.getLong("user_id");
        subscriptionList.add(new Subscription(targetName, userId, expiryTime));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return subscriptionList;
  }

  public List<Subscription> getSubscriptions() {
    LOGGER.log("Getting all subscriptions.");
    List<Subscription> subscriptionList = new ArrayList<>();
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(GET_SUBSCRIPTIONS_SQL);
      ResultSet resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        String targetName = resultSet.getString("target");
        Timestamp expiryTime = resultSet.getTimestamp("expiry");
        long userId = resultSet.getLong("user_id");
        subscriptionList.add(new Subscription(targetName, userId, expiryTime));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return subscriptionList;
  }

  public boolean addSubscription(String targetName, long userId) {
    LOGGER.log("Adding subscription to " + targetName + " for " + userId);
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(ADD_SUBSCRIPTION_SQL);
      preparedStatement.setLong(1, userId);
      preparedStatement.setString(2, targetName);
      preparedStatement.execute();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean removeSubscription(String targetName, long userId) {
    LOGGER.log("Removing subscription to " + targetName + " for " + userId);
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(REMOVE_SUBSCRIPTION_SQL);
      preparedStatement.setLong(1, userId);
      preparedStatement.setString(2, targetName);
      preparedStatement.execute();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean markSubscriptionNotified(String targetName, long userId) {
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(MARK_SUBSCRIPTION_NOTIFIED_SQL);
      preparedStatement.setLong(1, userId);
      preparedStatement.setString(2, targetName);
      preparedStatement.execute();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean cleanExpiredSubscriptions() {
    //LOGGER.log("Cleaning up expired subscriptions.");
    try {
      PreparedStatement preparedStatement =
          getConnection().prepareStatement(CLEAN_EXPIRED_SUBSCRIPTIONS_SQL);
      int resultSet = preparedStatement.executeUpdate();
      if (resultSet > 0) {
        LOGGER.log("Cleaned up " + resultSet + " expired subscriptions.");
      }
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  private Connection getConnection() {
    Config config = Config.getConfig();
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

  public static class Subscription {

    public final String targetName;
    public final Timestamp expiryTime;
    public final long userId;

    public Subscription(String targetName, long userId, Timestamp expiryTime) {
      this.targetName = targetName;
      this.userId = userId;
      this.expiryTime = expiryTime;
    }
  }
}
