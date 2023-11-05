package com.edmazur.eqrs.discord.commands.raidtarget;

import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordSlashSubCommand;
import com.edmazur.eqrs.game.RaidTarget;
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

    List<RaidTarget> targetList = Discord.getRaidTargets().getAll();
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
    addArgument(targetNameArgument);
  }

  public void onAction(CommandActionEvent event) {
    ArgumentResult[] args = event.getArguments();
    CommandResponder responder = event.getResponder();

    String targetName = args[1].get();

    responder.respondNow()
        .setContent("Subscribing to " + targetName)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
