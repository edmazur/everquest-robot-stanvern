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
import org.javacord.api.entity.emoji.Emoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageSet;
import org.javacord.api.entity.message.Reaction;

public class GratsAutoParseStats {

  public static void main(String[] args) {
    Config config = new Config();

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone(config.getString(Property.TIMEZONE_GUILD)));
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

    Discord discord = new Discord(config);

    Optional<MessageSet> maybeMessageSet =
        discord.getMessagesBetween(DiscordChannel.GG_TICKS_AND_GRATS, start, end);
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

      boolean hasRobot = false;
      boolean hasX = false;
      boolean hasOther = false;
      for (Reaction reaction : message.getReactions()) {
        Emoji emoji = reaction.getEmoji();
        if (emoji.equalsEmoji("🤖")) {
          hasRobot = true;
        } else if (emoji.equalsEmoji("❌")) {
          hasX = true;
        } else {
          hasOther = true;
        }
      }

      LocalDate localDate = LocalDate.ofInstant(
          message.getCreationTimestamp(),
          ZoneId.of(config.getString(Property.TIMEZONE_GUILD)));
      if (!datesToCounters.containsKey(localDate)) {
        datesToCounters.put(localDate, new Counters());
      }
      Counters counters = datesToCounters.get(localDate);
      if (hasRobot) {
        counters.parsed++;
      } else if (hasX) {
        counters.ignored++;
      } else if (hasOther) {
        counters.notParsed++;
      } else {
        counters.notYetHandled++;
      }
    }

    System.out.println(
        "date,"
        + "parsed,"
        + "not parsed,"
        + "ignored,"
        + "not yet handled");
    for (Map.Entry<LocalDate, Counters> mapEntry : datesToCounters.entrySet()) {
      LocalDate localDate = mapEntry.getKey();
      Counters counters = mapEntry.getValue();
      System.out.println(
          localDate + ","
          + counters.parsed + ","
          + counters.notParsed + ","
          + counters.ignored + ","
          + counters.notYetHandled);
    }

    System.exit(0);
  }


  private static class Counters {

    public int parsed;
    public int notParsed;
    public int ignored;
    public int notYetHandled;

  }

}
