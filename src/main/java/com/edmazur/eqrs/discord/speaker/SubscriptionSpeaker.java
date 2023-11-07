package com.edmazur.eqrs.discord.speaker;

import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordRole;
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
  private static final Logger LOGGER = new Logger();

  public SubscriptionSpeaker() {
  }

  @Override
  public void run() {
    Instant now = Instant.now();
    Database.getDatabase().cleanExpiredSubscriptions();

    // Get all subscriptions from the DB
    List<Database.Subscription> subscriptionList =
        Database.getDatabase().getSubscriptionsForNotification();
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
    for (RaidTarget raidTarget : RaidTargets.getAll()) {
      Window activeWindow = Window.getActiveWindow(raidTarget.getWindows(), now);
      Window.Status status = activeWindow.getStatus(now);
      if (status == Window.Status.SOON
          && activeWindow.getStart().isBefore(now.plus(Duration.ofMinutes(30)))) {
        String targetName = raidTarget.getName();
        // Check for subscriptions for the upcoming window
        if (subscriptionMap.containsKey(targetName)) {
          String message = ":boom:**Target Notification**:boom: `" + targetName
              + "` entering window <t:" + activeWindow.getStart().getEpochSecond() + ":R>";
          // Send a notification for each subscription
          for (long userId : subscriptionMap.get(targetName)) {
            if (Discord.getDiscord().isUserAuthorized(userId, DiscordRole.MEMBER)) {
              // Send a discord message
              messages.add(Discord.getDiscord().sendMessage(userId, message));
              // Mark as sent
              Database.getDatabase().markSubscriptionNotified(targetName, userId);
            } else {
              LOGGER.log("User did not have the required role, removing watch.");
              Database.getDatabase().removeSubscription(targetName, userId);
            }
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
