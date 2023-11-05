package com.edmazur.eqrs.discord.commands.raidtarget;

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
    // TODO: Get current active subscriptions for this user from the DB
    String[] subscriptions = {
        // Testing Examples
        "Cazic Thule",
        "Trakanon",
        "Lodizal",
        "Avatar of War"
    };

    List<EmbedBuilder> embeds = new ArrayList<>();
    for (String target : subscriptions) {
      // TODO: Embeds may be too heavyweight for this considering there are only two pieces of info
      EmbedBuilder embed = new EmbedBuilder()
          .setTitle(target)
          .setDescription("Expires at " + "TODO: expiration date");
      embeds.add(embed);
      // TODO: button or reaction based responses to each existing subscription
      // * Unsubscribe
      // * Refresh
    }

    CommandResponder responder = event.getResponder();
    responder.respondNow()
        .addEmbeds(embeds)
        .setFlags(MessageFlag.EPHEMERAL)
        .respond();
  }
}
