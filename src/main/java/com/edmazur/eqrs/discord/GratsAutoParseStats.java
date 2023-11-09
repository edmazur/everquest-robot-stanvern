// $ ./gradlew runGratsAutoParseStats --args='YYYYMMDD YYYYMMDD'

package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;

public class GratsAutoParseStats {

  public static void main(String[] args) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    simpleDateFormat.setTimeZone(
        TimeZone.getTimeZone(Config.getConfig().getString(Property.TIMEZONE_GUILD)));
    Instant start = null;
    Instant end = null;
    try {
      start = simpleDateFormat.parse(args[0]).toInstant();
      end = simpleDateFormat.parse(args[1]).toInstant();
    } catch (ParseException e) {
      e.printStackTrace();
      System.err.println("Couldn't parse one of the input dates");
      System.exit(-1);
    }

    Optional<MessageSet> maybeMessageSet =
        Discord.getDiscord().getMessagesBetween(DiscordChannel.GG_TICKS_AND_GRATS, start, end);
    if (maybeMessageSet.isEmpty()) {
      System.err.println("Error getting messages");
      System.exit(-1);
    }
    MessageSet messageSet = maybeMessageSet.get();

    Map<LocalDate, Counters> datesToCounters = new TreeMap<LocalDate, Counters>();
    for (Message message : messageSet) {
      if (!message.getAuthor().isYourself()) {
        continue;
      }

      String content = message.getContent();
      boolean parsedLootCommand = content.contains("✅ **$loot command");
      boolean parsedChannelMatch = content.contains("✅ **Channel match");

      LocalDate localDate = LocalDate.ofInstant(
          message.getCreationTimestamp(),
          ZoneId.of(Config.getConfig().getString(Property.TIMEZONE_GUILD)));
      if (!datesToCounters.containsKey(localDate)) {
        datesToCounters.put(localDate, new Counters());
      }
      Counters counters = datesToCounters.get(localDate);
      if (parsedLootCommand && parsedChannelMatch) {
        counters.parsedLootCommandAndChannelMatch++;
      } else if (parsedLootCommand) {
        counters.parsedOnlyLootCommand++;
      } else if (parsedChannelMatch) {
        counters.parsedOnlyChannelMatch++;
      } else {
        counters.parsedNeither++;
      }
    }

    System.out.println(
        "date,"
        + "parsed,"
        + "$loot parse failed,"
        + "channel match failed,"
        + "both failed");
    for (Map.Entry<LocalDate, Counters> mapEntry : datesToCounters.entrySet()) {
      LocalDate localDate = mapEntry.getKey();
      Counters counters = mapEntry.getValue();
      System.out.println(
          localDate + ","
          + counters.parsedLootCommandAndChannelMatch + ","
          + counters.parsedOnlyChannelMatch + ","
          + counters.parsedOnlyLootCommand + ","
          + counters.parsedNeither);
    }

    System.exit(0);
  }

  private static class Counters {

    public int parsedLootCommandAndChannelMatch;
    public int parsedOnlyLootCommand;
    public int parsedOnlyChannelMatch;
    public int parsedNeither;

  }

}
