package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.ParkLocation;
import com.edmazur.eqrs.game.ParkLocations;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DiscordParkListener implements MessageCreateListener {

  private static final Logger LOGGER = new Logger();
  private static final String PARKED_COMMAND = "!parked";

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_PARKED;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  public DiscordParkListener() {
    Discord.getDiscord().addListener(this);
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (!getChannel().isEventChannel(event)) {
      return;
    }

    if (event.getMessageContent().startsWith(PARKED_COMMAND)) {
      handleParkedCommand(event);
    }
  }

  private void handleParkedCommand(MessageCreateEvent event) {
    // Basic input checks.
    String input = event.getMessageContent().replace(PARKED_COMMAND, "").trim();
    String[] parts = input.split("\\s+");
    if (parts.length < 2) {
      sendReply(event, "❌ Usage: <park location> <bot name>");
      return;
    }

    // Get and remove bot name.
    final String botName = StringUtils.capitalize(parts[parts.length - 1].toLowerCase());
    input = input.replaceAll(parts[parts.length - 1], "").trim();

    // Parse park location.
    Optional<ParkLocations> maybeParkLocations = Database.getDatabase().getParkLocations();
    if (maybeParkLocations.isEmpty()) {
      sendReply(event, "❌ Error reading from database");
      return;
    }
    ParkLocations parkLocations = maybeParkLocations.get();
    Optional<ParkLocation> maybeParkLocation = parkLocations.getParkLocation(input);
    if (maybeParkLocation.isEmpty()) {
      sendReply(event, String.format("❌ Unrecognized park location: `%s`", input));
      return;
    }
    ParkLocation parkLocation = maybeParkLocation.get();

    // Update database.
    if (!Database.getDatabase().updateBotLocation(botName, parkLocation)) {
      sendReply(event, "❌ Error updating database");
      return;
    }

    // Success.
    sendReply(
        event,
        String.format(
            "✅ Park location for `%s` updated to `%s`",
            botName, parkLocation.getName()));
  }

  private void sendReply(MessageCreateEvent event, String content) {
    new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .setContent(content)
        .send(event.getChannel());
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().isDebug()) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
