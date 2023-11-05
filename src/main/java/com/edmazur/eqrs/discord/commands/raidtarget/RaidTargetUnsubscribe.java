package com.edmazur.eqrs.discord.commands.raidtarget;

import com.edmazur.eqrs.discord.DiscordSlashSubCommand;
import me.s3ns3iw00.jcommands.CommandResponder;
import me.s3ns3iw00.jcommands.argument.ArgumentResult;
import me.s3ns3iw00.jcommands.argument.autocomplete.Autocomplete;
import me.s3ns3iw00.jcommands.argument.type.StringArgument;
import me.s3ns3iw00.jcommands.argument.util.Choice;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;
import org.javacord.api.entity.message.MessageFlag;

public class RaidTargetUnsubscribe extends DiscordSlashSubCommand {

  public RaidTargetUnsubscribe() {
    super("unsubscribe", "Unsubscribe from a raid target timer");

    // Get current active subscriptions for this user
    Choice[] subscriptions = {
        // Examples
        new Choice("Grik", "Grik"),
        new Choice("Avatar of War", "Avatar of War")
    };

    // Let them pick from those subscriptions
    // The names for the args must be unique or else autocompletes act funky
    // see: https://github.com/S3nS3IW00/JCommands/issues/22
    StringArgument<String> targetNameArgument = new StringArgument<>(
        "subscription", "The name of the raid target", String.class);
    Autocomplete autocomplete = new Autocomplete(subscriptions);
    targetNameArgument.addAutocomplete(autocomplete);
    addArgument(targetNameArgument);
  }

  public void onAction(CommandActionEvent event) {
    ArgumentResult[] args = event.getArguments();
    CommandResponder responder = event.getResponder();

    String targetName = args[1].get();

    responder.respondNow()
        .setContent("Unsubscribing from " + targetName)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
