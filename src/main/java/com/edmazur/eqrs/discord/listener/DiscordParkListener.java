package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Database;
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

  private static final String PARKED_COMMAND = "!parked";

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_PARKED;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;
  private final Database database;

  public DiscordParkListener(
      Config config,
      Discord discord,
      Database database) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.database = database;
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
    Optional<ParkLocations> maybeParkLocations = database.getParkLocations();
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
    if (!database.updateBotLocation(botName, parkLocation)) {
      sendReply(event, "❌ Error updating database");
      return;
    }

    // Success.
    sendReply(
        event,
        String.format(
            "✅ Park location for `%s` updated to `%s`", botName, parkLocation.getName()));
  }

  private void sendReply(MessageCreateEvent event, String content) {
    new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .setContent(content)
        .send(event.getChannel());
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
