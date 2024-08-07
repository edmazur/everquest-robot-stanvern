package com.edmazur.eqrs.discord;

import java.util.List;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public enum DiscordChannel {

  // Good Guys server.
  GG_BATPHONE(1007149490089758760L),
  GG_BOT_CAMP(1018018359725539409L),
  GG_BOT_TALK(1117810040586387466L),
  GG_GMOTD(1007148929856589925L),
  GG_GROUP_TEXT(1014653907353477200L),
  GG_IMPORTANT_AUDIT(1007151112572387438L),
  GG_PARKED(1121901216214024262L),
  GG_PHONE_AUDIT(1007149647380369459L),
  GG_QUAKEPHONE(1267923572429688933L),
  GG_GENERAL_CHAT(1007152136758513704L),
  GG_RAID_DISCUSSION(1018932338404626503L),
  GG_TICKS_AND_GRATS(1007177548305809529L),
  GG_TIMERS(1007177226711744532L),
  GG_TOD(1007177147263221770L),

  // Test server.
  TEST_ANNOUNCEMENTS(1000585605551505498L),
  TEST_BATPHONE(953825205820731443L),
  TEST_BOT_SCRAPE(1115499314869973012L),
  TEST_GENERAL(978166277627580426L),
  TEST_IMPORTANT_AUDIT(1000585640196444160L),
  TEST_TIMERS(978484579948167178L),
  TEST_TOD(1000579500159012884L),
  TEST_UNAUDITED(1018118632531832964L),

  ;

  private final Long id;

  private DiscordChannel(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public boolean isEventChannel(MessageCreateEvent event) {
    return isEventChannel(event.getChannel());
  }

  public boolean isEventChannel(TextChannel channel) {
    return channel.getId() == id;
  }

  public static boolean containsEventChannel(
      MessageCreateEvent event, List<DiscordChannel> haystack) {
    return containsEventChannel(event.getChannel(), haystack);
  }

  public static boolean containsEventChannel(TextChannel needle, List<DiscordChannel> haystack) {
    for (DiscordChannel hay : haystack) {
      if (hay.getId() == needle.getId()) {
        return true;
      }
    }
    return false;
  }

}
