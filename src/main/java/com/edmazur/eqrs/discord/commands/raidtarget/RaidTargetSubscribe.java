package com.edmazur.eqrs.discord.commands.raidtarget;

import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordRole;
import com.edmazur.eqrs.discord.DiscordSlashSubCommand;
import com.edmazur.eqrs.game.RaidTarget;
import com.edmazur.eqrs.game.RaidTargets;
import java.util.ArrayList;
import java.util.List;
import me.s3ns3iw00.jcommands.CommandResponder;
import me.s3ns3iw00.jcommands.argument.ArgumentResult;
import me.s3ns3iw00.jcommands.argument.autocomplete.type.SearchAutocomplete;
import me.s3ns3iw00.jcommands.argument.type.StringArgument;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;
import org.javacord.api.entity.message.MessageFlag;

public class RaidTargetSubscribe extends DiscordSlashSubCommand {

  public RaidTargetSubscribe() {
    super("subscribe", "Subscribe to a raid target timer");

    List<RaidTarget> targetList = RaidTargets.getAll();
    List<Object> targetStringList = new ArrayList<>();
    for (RaidTarget target : targetList) {
      targetStringList.add(target.getName());
      // Also add aliases to search? might be simpler to not
      //targetStringList.addAll(target.getAliases());
    }

    // The names for the args must be unique or else autocompletes act funky
    // see: https://github.com/S3nS3IW00/JCommands/issues/22
    StringArgument<String> targetNameArgument = new StringArgument<>(
        "target", "The name of the raid target", String.class);
    SearchAutocomplete searchAutocomplete = new SearchAutocomplete(
        SearchAutocomplete.SearchType.CONTAINS, targetStringList
    ).ignoreCase().limit(10).sort(SearchAutocomplete.SortType.ASCENDING).minCharToSearch(2);
    targetNameArgument.addAutocomplete(searchAutocomplete);
    targetNameArgument.getArgumentValidator().when(value -> ! targetStringList.contains(value))
        .thenRespond(event -> {
          event.getResponder().respondNow()
              .setContent("Provided target name not valid! Please provide one from the list.")
              .setFlags(MessageFlag.EPHEMERAL)
              .respond();
        });
    addArgument(targetNameArgument);
  }

  public void onAction(CommandActionEvent event) {
    ArgumentResult[] args = event.getArguments();
    CommandResponder responder = event.getResponder();

    long userId = event.getSender().getId();
    String message;

    if (Discord.getDiscord().isUserAuthorized(userId, DiscordRole.MEMBER)) {
      String targetName = args[1].get();
      boolean success = Database.getDatabase().addSubscription(targetName, userId);

      if (success) {
        message = "Subscribed to `" + targetName + "`.";
      } else {
        message = "Failed to subscribe to `" + targetName + "`. "
            + "Either this subscription already exists, or something went *horribly wrong* "
            + "and everything is about to be on fire.";
      }
    } else {
      message = "You are not authorized to subscribe to targets.";
    }
    responder.respondNow()
        .setContent(message)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
