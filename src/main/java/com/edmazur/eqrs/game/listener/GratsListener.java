package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqlp.EqLogListener;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.util.List;
import org.javacord.api.entity.message.MessageBuilder;

public class GratsListener implements EqLogListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.FOW_RAID_TICKS_AND_GRATSS;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private final Config config;
  private final Discord discord;
  private final GratsDetector gratsDetector;
  private final ItemDatabase itemDatabase;
  private final ItemScreenshotter itemScreenshotter;

  public GratsListener(
      Config config,
      Discord discord,
      GratsDetector gratsDetector,
      ItemDatabase itemDatabase,
      ItemScreenshotter itemScreenshotter) {
    this.config = config;
    this.discord = discord;
    this.gratsDetector = gratsDetector;
    this.itemDatabase = itemDatabase;
    this.itemScreenshotter = itemScreenshotter;
  }

  @Override
  public void onEvent(EqLogEvent eqLogEvent) {
    if (gratsDetector.containsGrats(eqLogEvent)) {
      List<Item> items = itemDatabase.parse(eqLogEvent.getPayload());

      // TODO: Factor out the code that's repeated here and in ItemListener.
      MessageBuilder messageBuilder = new MessageBuilder()
          .append("💰 Possible gratss sighting, ET: `" + eqLogEvent.getFullLine() + "`");
      for (Item item : items) {
        messageBuilder.append("\n" + item.getName() + " (" + item.getUrl() + ")");
      }
      // Add the attachments in reverse order so that they appear in the same order as the names.
      // Probably a Javacord bug.
      for (int i = items.size() - 1; i >= 0; i--) {
        Item item = items.get(i);
        messageBuilder.addAttachment(itemScreenshotter.get(item));
      }
      discord.sendMessage(getChannel(), messageBuilder);
    }
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
