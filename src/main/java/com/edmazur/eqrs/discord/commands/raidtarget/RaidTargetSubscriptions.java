package com.edmazur.eqrs.discord.commands.raidtarget;

import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.discord.DiscordSlashSubCommand;
import java.util.ArrayList;
import java.util.List;
import me.s3ns3iw00.jcommands.CommandResponder;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class RaidTargetSubscriptions extends DiscordSlashSubCommand {

  public RaidTargetSubscriptions() {
    super("subscriptions", "List raid target timer subscriptions");
  }

  public void onAction(CommandActionEvent event) {
    long userId = event.getSender().getId();
    List<Database.Subscription> subscriptionList =
        Database.getDatabase().getSubscriptionsForUser(userId);

    List<EmbedBuilder> embeds = new ArrayList<>();
    for (Database.Subscription subscription : subscriptionList) {
      long expirySeconds = subscription.expiryTime.toInstant().getEpochSecond();
      // TODO: Embeds may be too heavyweight for this considering there are only two pieces of info
      EmbedBuilder embed = new EmbedBuilder()
          .setTitle(subscription.targetName)
          .setDescription("Expires <t:" + expirySeconds + ":R>");
      embeds.add(embed);
      // TODO: button or reaction based responses to each existing subscription
      // * Unsubscribe
      // * Refresh
    }

    CommandResponder responder = event.getResponder();
    responder.respondNow()
        .setContent(!embeds.isEmpty() ? "Subscriptions:" : "No subscriptions.")
        .addEmbeds(embeds)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
