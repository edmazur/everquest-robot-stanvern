package com.edmazur.eqrs.game.listeners;

import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordPredicate;
import com.edmazur.eqrs.discord.DiscordUser;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;

public class LootGroupExpander {

  private static final Logger LOGGER = new Logger();


  private LootGroupExpander() {
    throw new IllegalStateException("Cannot be instantiated");
  }

  public static Map<String, String> getExpansions() {
    Optional<Message> maybeLootGroupMessage = Discord.getDiscord().getLastMessageMatchingPredicate(
        DiscordChannel.GG_BOT_TALK,
        DiscordPredicate.isFromUser(DiscordUser.ALFRED).and(DiscordPredicate.hasAttachment()));
    if (maybeLootGroupMessage.isEmpty()) {
      LOGGER.log("Error finding loot group definitions");
      return Map.of();
    }
    Message lootGroupMessage = maybeLootGroupMessage.get();

    Map<String, List<String>> lootGroupsToLootLists = Maps.newHashMap();
    for (MessageAttachment messageAttachment : lootGroupMessage.getAttachments()) {
      try {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(messageAttachment.asInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split(",");
          if (parts.length != 2) {
            LOGGER.log("Error parsing line: %s", line);
          }
          String lootGroup = parts[0];
          String loot = parts[1];
          if (!lootGroupsToLootLists.containsKey(lootGroup)) {
            lootGroupsToLootLists.put(lootGroup, Lists.newArrayList());
          }
          lootGroupsToLootLists.get(lootGroup).add(loot);
        }
      } catch (IOException e) {
        LOGGER.log("Error reading attachment for loot group definitions");
        e.printStackTrace();
        return Map.of();
      }
    }

    Map<String, String> lootGroupsToLootStrings = Maps.newHashMap();
    for (Map.Entry<String, List<String>> mapEntry : lootGroupsToLootLists.entrySet()) {
      String lootGroup = mapEntry.getKey();
      List<String> lootList = mapEntry.getValue();
      lootGroupsToLootStrings.put(lootGroup, StringUtils.join(lootList, "\n"));
    }
    return lootGroupsToLootStrings;
  }

}
