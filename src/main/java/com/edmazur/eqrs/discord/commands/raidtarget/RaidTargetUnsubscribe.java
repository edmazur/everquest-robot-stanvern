package com.edmazur.eqrs.discord.commands.raidtarget;

import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.discord.DiscordSlashSubCommand;
import java.util.ArrayList;
import java.util.List;
import me.s3ns3iw00.jcommands.CommandResponder;
import me.s3ns3iw00.jcommands.argument.ArgumentResult;
import me.s3ns3iw00.jcommands.argument.autocomplete.type.ConditionalAutocomplete;
import me.s3ns3iw00.jcommands.argument.type.StringArgument;
import me.s3ns3iw00.jcommands.argument.util.Choice;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;
import org.javacord.api.entity.message.MessageFlag;

public class RaidTargetUnsubscribe extends DiscordSlashSubCommand {

  public RaidTargetUnsubscribe() {
    super("unsubscribe", "Unsubscribe from a raid target timer");

    // The names for the args must be unique or else autocompletes act funky
    // see: https://github.com/S3nS3IW00/JCommands/issues/22
    StringArgument<String> targetNameArgument = new StringArgument<>(
        "subscription", "The name of the raid target", String.class);
    ConditionalAutocomplete autocomplete = new ConditionalAutocomplete(state -> {
      long userId = state.getSender().getId();
      // Get the user's subscriptions
      List<Database.Subscription> subscriptionList =
          Database.getDatabase().getSubscriptionsForUser(userId);
      List<Choice> choiceList = new ArrayList<>();
      for (Database.Subscription subscription : subscriptionList) {
        // Let them pick from those subscriptions
        choiceList.add(new Choice(subscription.targetName, subscription.targetName));
      }
      return choiceList;
    });
    targetNameArgument.addAutocomplete(autocomplete);
    addArgument(targetNameArgument);
  }

  public void onAction(CommandActionEvent event) {
    ArgumentResult[] args = event.getArguments();
    CommandResponder responder = event.getResponder();

    String targetName = args[1].get();
    long userId = event.getSender().getId();
    boolean success = Database.getDatabase().removeSubscription(targetName, userId);
    String message;
    if (success) {
      message = "Unsubscribed from " + targetName + ".";
    } else {
      message = "Failed to unsubscribe from " + targetName + ". "
          + "Either this subscription doesn't exist, or something went *horribly wrong* "
          + "and everything is about to be on fire.";
    }
    responder.respondNow()
        .setContent(message)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
