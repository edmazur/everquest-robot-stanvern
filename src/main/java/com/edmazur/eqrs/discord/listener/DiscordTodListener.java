package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Database;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.game.RaidTarget;
import com.edmazur.eqrs.game.RaidTargets;
import java.awt.Color;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class DiscordTodListener implements MessageCreateListener {

  private static final String HELP_TOD_USAGE =
      "- !tod usage: `!tod <target>, <timestamp>` Example: `!tod naggy, 5/14 17:55:36`";
  private static final String HELP_QUAKE_USAGE =
      "- !quake usage: `!quake <timestamp>` Example: `!quake 5/14 17:55:36`";
  private static final String HELP_TARGET =
      "- `<target>` is case-insensitive and supports common abbrevations, "
      + "e.g. `Lord Nagafen`, `lord nagafen`, and `naggy` are all valid.";
  private static final String HELP_TIMESTAMP =
      "- `<timestamp>` format is `{month}/{day} {hour}:{minute}:{seconds}`. "
      + "Times are 24h format (e.g. 7pm = 19:00:00) and are always interpreted as Eastern time.";
  private static final String HELP_GUILD =
      "- `<guild>` is case-insensitive and supports common abbrevations, "
      + "e.g. `Force of Will` and `fow` are both valid.";

  private static final Pattern HELP_PATTERN = Pattern.compile("!help.*");
  private static final Pattern TIMESTAMP_PATTERN =
      Pattern.compile("(\\d+{1,2})/(\\d+{1,2}) (\\d{1,2}):(\\d{2}):(\\d{2})");

  private static final Pattern TOD_PATTERN = Pattern.compile("!tod.*");
  private static final Pattern TOD_PARSE_PATTERN = Pattern.compile("!tod (.+), (.+)");

  private static final Pattern QUAKE_PATTERN = Pattern.compile("!quake.*");
  private static final Pattern QUAKE_PARSE_PATTERN = Pattern.compile("!quake (.+)");

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TOD;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_TOD;

  private static final File SUCCESS_IMAGE = new File("src/main/resources/str.png");

  private final Config config;
  private final Discord discord;
  private final Database database;
  private final RaidTargets raidTargets;

  public DiscordTodListener(
      Config config,
      Discord discord,
      Database database,
      RaidTargets raidTargets) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.database = database;
    this.raidTargets = raidTargets;
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    if (getChannel().isEventChannel(event)) {
      Matcher helpMatcher = HELP_PATTERN.matcher(event.getMessageContent());
      if (helpMatcher.matches()) {
        sendReply(event,
            HELP_TOD_USAGE
            + "\n" + HELP_QUAKE_USAGE
            + "\n" + HELP_TARGET
            + "\n" + HELP_TIMESTAMP
            + "\n" + HELP_GUILD);
        return;
      }

      Matcher todMatcher = TOD_PATTERN.matcher(event.getMessageContent());
      if (todMatcher.matches()) {
        int commaCount = 0;
        for (int i = 0; i < event.getMessageContent().length(); i++) {
          if (event.getMessageContent().charAt(i) == ',') {
            commaCount++;
          }
        }
        if (commaCount != 1) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, unrecognized !tod command, there should be exactly 1 comma"
              + "\n" + HELP_TOD_USAGE
              + "\n" + HELP_TARGET
              + "\n" + HELP_TIMESTAMP);
          return;
        }

        Matcher todParseMatcher = TOD_PARSE_PATTERN.matcher(event.getMessageContent());

        // !tod was used, but arguments could not be parsed.
        if (!todParseMatcher.matches() || todParseMatcher.groupCount() != 2) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, unrecognized !tod command"
              + "\n" + HELP_TOD_USAGE
              + "\n" + HELP_TARGET
              + "\n" + HELP_TIMESTAMP);
          return;
        }

        // Parse target.
        String raidTargetToParse = todParseMatcher.group(1);
        Optional<RaidTarget> maybeRaidTarget = raidTargets.getRaidTarget(raidTargetToParse);
        if (maybeRaidTarget.isEmpty()) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, I don't know this target: `" + raidTargetToParse + "`"
              + "\n" + HELP_TARGET);
          return;
        }
        // This is final to avoid a VariableDeclarationUsageDistance checkstyle warning.
        // TODO: Disable that check?
        final RaidTarget raidTarget = maybeRaidTarget.get();

        // Parse timestamp.
        String timestampToParse = todParseMatcher.group(2);
        Optional<LocalDateTime> maybeTimestamp = getTimestamp(timestampToParse);
        if (maybeTimestamp.isEmpty()) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, I can't read this timestamp: `" + timestampToParse + "`"
              + "\n" + HELP_TIMESTAMP);
          return;
        }
        LocalDateTime timestamp = maybeTimestamp.get();
        if (timestamp.isAfter(LocalDateTime.now())) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, ToD cannot be in the future: `" + timestampToParse + "`");
          return;
        }
        LocalDateTime quakeTime = database.getQuakeTime();
        if (timestamp.isBefore(quakeTime)) {
          event.addReactionsToMessage("❌");
          sendReply(event,
              "Sorry, ToD cannot be before quake time (" + DATE_TIME_FORMATTER.format(quakeTime)
                  + " ET)");
          return;
        }
        String timeSince = "~" + getTimeSince(timestamp) + " ago";

        // Create embed response.
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Success!")
            .setColor(Color.GREEN)
            .setThumbnail(SUCCESS_IMAGE)
            .addField("http://edmazur.com/eq updated",
                  "`     Target:` " + raidTarget.getName() + "\n"
                + "`   (ET) ToD:` " + DATE_TIME_FORMATTER.format(timestamp)
                + " [" + timeSince + "]\n"
                + "`(local) ToD:` " + "<t:" + getUnixTimestamp(timestamp) + ":F>");

        database.updateTimeOfDeath(raidTarget, timestamp);
        event.addReactionsToMessage("👍");
        sendReply(event, embed);
      }

      Matcher quakeMatcher = QUAKE_PATTERN.matcher(event.getMessageContent());
      if (quakeMatcher.matches()) {
        Matcher quakeParseMatcher = QUAKE_PARSE_PATTERN.matcher(event.getMessageContent());

        // !quake was used, but argument could not be parsed.
        if (!quakeParseMatcher.matches() || quakeParseMatcher.groupCount() != 1) {
          event.addReactionsToMessage("❌");
          event.getMessage().reply("Sorry, unrecognized !quake command"
              + "\n" + HELP_QUAKE_USAGE
              + "\n" + HELP_TIMESTAMP);
          return;
        }

        // Parse timestamp.
        String timestampToParse = quakeParseMatcher.group(1);
        Optional<LocalDateTime> maybeTimestamp = getTimestamp(timestampToParse);
        if (maybeTimestamp.isEmpty()) {
          event.addReactionsToMessage("❌");
          event.getMessage().reply("Sorry, I can't read this timestamp: `" + timestampToParse + "`"
              + "\n" + HELP_TIMESTAMP);
          return;
        }
        LocalDateTime timestamp = maybeTimestamp.get();
        if (timestamp.isAfter(LocalDateTime.now())) {
          event.addReactionsToMessage("❌");
          event.getMessage().reply(
              "Sorry, quake time cannot be in the future: `" + timestampToParse + "`");
          return;
        }
        String timeSince = "~" + getTimeSince(timestamp) + " ago";

        // Create embed response.
        EmbedBuilder embed = new EmbedBuilder()
            .setTitle("Success!")
            .setColor(Color.GREEN)
            .setThumbnail(SUCCESS_IMAGE)
            .addField("http://edmazur.com/eq updated",
                "`   (ET) Quake time:` " + DATE_TIME_FORMATTER.format(timestamp)
                + " [" + timeSince + "]\n"
                + "`(local) Quake time:` " + "<t:" + getUnixTimestamp(timestamp) + ":F>");

        database.updateQuakeTime(timestamp);
        event.addReactionsToMessage("👍");
        event.getMessage().reply(embed);
      }
    }
  }

  private void sendReply(MessageCreateEvent event, String content) {
    new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .setContent(content)
        .send(event.getChannel());
  }

  private void sendReply(MessageCreateEvent event, EmbedBuilder embed) {
    new MessageBuilder()
        .replyTo(event.getMessage())
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .setEmbed(embed)
        .send(event.getChannel());
  }

  private Optional<LocalDateTime> getTimestamp(String timestampToParse) {
    Matcher matcher = TIMESTAMP_PATTERN.matcher(timestampToParse);
    if (!matcher.matches() || matcher.groupCount() != 5) {
      return Optional.empty();
    }
    int month = Integer.parseInt(matcher.group(1));
    int day = Integer.parseInt(matcher.group(2));
    int hour = Integer.parseInt(matcher.group(3));
    int minute = Integer.parseInt(matcher.group(4));
    int seconds = Integer.parseInt(matcher.group(5));

    // Special handling for start of year when entering ToDs from last year.
    int year = LocalDateTime.now().getYear();
    if (month == 12 && LocalDateTime.now().getMonthValue() == 1) {
      year--;
    }

    return Optional.of(LocalDateTime.of(year, month, day, hour, minute, seconds));
  }

  private String getTimeSince(LocalDateTime localDateTime) {
    Duration duration = Duration.between(localDateTime, LocalDateTime.now());
    int seconds = (int) duration.getSeconds();

    if (seconds < 60) {
      return seconds + " " + singular(seconds, "second");
    }

    int minutes = seconds / 60;
    if (minutes < 60) {
      return minutes + " " + singular(minutes, "minute");
    }

    int hours = minutes / 60;
    if (hours < 48) {
      return hours + " " + singular(hours, "hour");
    }

    int days = hours / 24;
    hours -= days * 24;
    return days + " " + singular(days, "day") + ", " + hours + " " + singular(hours, "hour");
  }

  private String singular(int value, String singular) {
    return singular(value, singular, null);
  }

  private String singular(int value, String singular, String plural) {
    if (plural == null) {
      if (value != 1) {
        return singular + "s";
      } else {
        return singular;
      }
    } else {
      if (value != 1) {
        return plural;
      } else {
        return singular;
      }
    }
  }

  private long getUnixTimestamp(LocalDateTime localDateTime) {
    return localDateTime.atZone(ZoneId.of("America/New_York")).toEpochSecond();
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

}
