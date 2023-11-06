package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.game.RaidTarget;
import com.edmazur.eqrs.game.RaidTargets;
import com.edmazur.eqrs.game.Window;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.javacord.api.entity.message.Message;

public class SubscriptionSpeaker implements Runnable {
  private final Discord discord;
  private final RaidTargets raidTargets;
  private final Database database;

  public SubscriptionSpeaker(
      Config config,
      Discord discord,
      RaidTargets raidTargets) {
    this.discord = discord;
    this.raidTargets = raidTargets;
    this.database = Database.getDatabase(config);
  }

  @Override
  public void run() {
    Instant now = Instant.now();
    database.cleanExpiredSubscriptions();

    // Get all subscriptions from the DB
    List<Database.Subscription> subscriptionList = database.getSubscriptionsForNotification();
    // Convert the list of subscriptions to a map
    Map<String, List<Long>> subscriptionMap = new HashMap<>();
    for (Database.Subscription subscription : subscriptionList) {
      List<Long> targetList;
      if (!subscriptionMap.containsKey(subscription.targetName)) {
        targetList = new ArrayList<>();
        targetList.add(subscription.userId);
        subscriptionMap.put(subscription.targetName, targetList);
      } else {
        targetList = subscriptionMap.get(subscription.targetName);
        targetList.add(subscription.userId);
      }
    }

    // Get upcoming windows (within 30m)
    List<CompletableFuture<Message>> messages = new ArrayList<>();
    for (RaidTarget raidTarget : raidTargets.getAll()) {
      Window activeWindow = Window.getActiveWindow(raidTarget.getWindows(), now);
      Window.Status status = activeWindow.getStatus(now);
      if (status == Window.Status.SOON
          && activeWindow.getStart().isBefore(now.plus(Duration.ofMinutes(30)))) {
        String targetName = raidTarget.getName();
        // Check for subscriptions for the upcoming window
        if (subscriptionMap.containsKey(targetName)) {
          String message = "Target Notification: " + targetName
              + " entering window at " + activeWindow.getStart();
          // Send a notification for each subscription
          for (long userId : subscriptionMap.get(targetName)) {
            // Send a discord message
            messages.add(discord.sendMessage(userId, message));
            // Mark as sent
            database.markSubscriptionNotified(targetName, userId);
          }
        }
      }
    }

    // Join all the messages after compiling them because... IDK
    // Maybe this is an over-optimization.
    for (CompletableFuture<Message> message : messages) {
      message.join();
    }
  }
}
