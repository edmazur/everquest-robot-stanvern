package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

public class LootStatusRequester implements Runnable {

  private static final DiscordChannel PROD_COMMAND_CHANNEL = DiscordChannel.GG_GROUP_TEXT;
  private static final DiscordChannel TEST_COMMAND_CHANNEL = DiscordChannel.TEST_GENERAL;

  public LootStatusRequester() { }

  @Override
  public void run() {
    Discord.getDiscord().sendMessage(getChannel(), "Nightly loot status check: !lootstatus");
  }

  private DiscordChannel getChannel() {
    if (Config.getConfig().getBoolean(Property.DEBUG)) {
      return TEST_COMMAND_CHANNEL;
    } else {
      return PROD_COMMAND_CHANNEL;
    }
  }

}
