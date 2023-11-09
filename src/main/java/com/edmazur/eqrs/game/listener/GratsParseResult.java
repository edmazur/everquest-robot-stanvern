package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.discord.DiscordButton;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageUpdater;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.entity.message.component.ButtonBuilder;
import org.javacord.api.entity.message.component.ButtonStyle;

public class GratsParseResult {

  private static final char SUCCESS_ICON = '✅';
  private static final char ERROR_ICON = '❌';
  // Note that this must be preceded by "//" in regexs.
  private static final String LOOT_COMMAND_LABEL = "$loot command";
  private static final String CHANNEL_MATCH_LABEL = "Channel match";

  private static final String RAW_GRATS_MESSAGE_FORMAT = "💰 ET: ``%s``";
  private static final Pattern RAW_GRATS_MESSAGE_PATTERN = Pattern.compile(
      "^💰 ET: ``(.+)``$");

  private static final String LOOT_COMMAND_SUCCESS_FORMAT =
      SUCCESS_ICON + " **" + LOOT_COMMAND_LABEL + "**: ``%s``";
  private static final Pattern LOOT_COMMAND_SUCCESS_PATTERN =
      Pattern.compile("^" + SUCCESS_ICON + " \\*\\*\\" + LOOT_COMMAND_LABEL + "\\*\\*: ``(.+)``$");
  private static final String LOOT_COMMAND_ERROR_FORMAT =
      ERROR_ICON + " **" + LOOT_COMMAND_LABEL + "**: %s";
  private static final Pattern LOOT_COMMAND_ERROR_PATTERN =
      Pattern.compile("^" + ERROR_ICON + " \\*\\*\\" + LOOT_COMMAND_LABEL + "\\*\\*: (.+)$");

  private static final String CHANNEL_MATCH_SUCCESS_FORMAT =
      SUCCESS_ICON + " **" + CHANNEL_MATCH_LABEL + "**: <#%d>";
  private static final Pattern CHANNEL_MATCH_SUCCESS_PATTERN =
      Pattern.compile("^" + SUCCESS_ICON + " \\*\\*" + CHANNEL_MATCH_LABEL
          + "\\*\\*: <#([0-9]+)>$");
  private static final String CHANNEL_MATCH_ERROR_FORMAT =
      ERROR_ICON + " **" + CHANNEL_MATCH_LABEL + "**: %s";
  private static final Pattern CHANNEL_MATCH_ERROR_PATTERN =
      Pattern.compile("^" + ERROR_ICON + " \\*\\*" + CHANNEL_MATCH_LABEL + "\\*\\*: (.+)$");

  private final EqLogEvent eqLogEvent;
  private final List<String> itemUrls;
  private final ValueOrError<String> lootCommandOrError;
  private final ValueOrError<Long> channelMatchOrError;
  private final List<ValueOrError<File>> itemScreenshotsOrErrors;

  public GratsParseResult(
      EqLogEvent eqLogEvent,
      List<String> itemUrls,
      ValueOrError<String> lootCommandOrError,
      ValueOrError<Long> channelMatchOrError,
      List<ValueOrError<File>> itemScreenshotsOrErrors) {
    this.eqLogEvent = eqLogEvent;
    this.itemUrls = itemUrls;
    this.lootCommandOrError = lootCommandOrError;
    this.channelMatchOrError = channelMatchOrError;
    this.itemScreenshotsOrErrors = itemScreenshotsOrErrors;
  }

  /**
   * Reconstructs a GratsParseResult from a Discord message.
   *
   * <p>Note that since these reconstructed objects are expected to be used only for editing
   * existing messages, and because we never expect to replace attachments of existing messages,
   * this method adds only errors to itemScreenshotsOrErrors (i.e. screenshots are omitted).
   */
  public static Optional<GratsParseResult> fromMessage(Message message) {
    // Do first round of parsing.
    String[] lines = message.getContent().split("\n");
    if (lines.length < 3) {
      return Optional.empty();
    }
    final String rawGratsMessageLine = lines[0];
    final String lootCommandOrErrorLine = lines[1];
    final String channelMatchOrErrorLine = lines[2];
    List<String> itemLinkOrScreenshotErrorLines = Lists.newArrayList();
    for (int i = 3; i < lines.length; i++) {
      itemLinkOrScreenshotErrorLines.add(lines[i]);
    }

    // Read raw !grats message.
    EqLogEvent eqLogEvent;
    Matcher matcher = RAW_GRATS_MESSAGE_PATTERN.matcher(rawGratsMessageLine);
    if (matcher.matches() && matcher.groupCount() == 1) {
      Optional<EqLogEvent> maybeEqLogEvent = EqLogEvent.parseFromLine(matcher.group(1));
      if (maybeEqLogEvent.isEmpty()) {
        return Optional.empty();
      }
      eqLogEvent = maybeEqLogEvent.get();
    } else {
      return Optional.empty();
    }

    // Read loot command or error.
    ValueOrError<String> lootCommandOrError;
    matcher = LOOT_COMMAND_SUCCESS_PATTERN.matcher(lootCommandOrErrorLine);
    if (matcher.matches() && matcher.groupCount() == 1) {
      lootCommandOrError = ValueOrError.value(matcher.group(1));
    } else {
      matcher = LOOT_COMMAND_ERROR_PATTERN.matcher(lootCommandOrErrorLine);
      if (matcher.matches() && matcher.groupCount() == 1) {
        lootCommandOrError = ValueOrError.error(matcher.group(1));
      } else {
        return Optional.empty();
      }
    }

    // Read channel match or error.
    ValueOrError<Long> channelMatchOrError;
    matcher = CHANNEL_MATCH_SUCCESS_PATTERN.matcher(channelMatchOrErrorLine);
    if (matcher.matches() && matcher.groupCount() == 1) {
      channelMatchOrError = ValueOrError.value(Long.valueOf(matcher.group(1)));
    } else {
      matcher = CHANNEL_MATCH_ERROR_PATTERN.matcher(channelMatchOrErrorLine);
      if (matcher.matches() && matcher.groupCount() == 1) {
        channelMatchOrError = ValueOrError.error(matcher.group(1));
      } else {
        return Optional.empty();
      }
    }

    // Read item links and screenshot errors.
    List<String> itemUrls = Lists.newArrayList();
    List<ValueOrError<File>> itemScreenshotsOrErrors = Lists.newArrayList();
    for (int i = 0; i < itemLinkOrScreenshotErrorLines.size(); i++) {
      String itemLinkOrScreenshotErrorLine = itemLinkOrScreenshotErrorLines.get(i);
      if (i < message.getAttachments().size()) {
        // It's an item link.
        itemUrls.add(itemLinkOrScreenshotErrorLine);
      } else {
        // It's a screenshot error.
        itemScreenshotsOrErrors.add(ValueOrError.error(itemLinkOrScreenshotErrorLine));
      }
    }

    return Optional.of(new GratsParseResult(
        eqLogEvent, itemUrls, lootCommandOrError, channelMatchOrError, itemScreenshotsOrErrors));
  }

  public EqLogEvent getLogEvent() {
    return eqLogEvent;
  }

  public ValueOrError<String> getLootCommandOrError() {
    return lootCommandOrError;
  }

  public ValueOrError<Long> getChannelMatchOrError() {
    return channelMatchOrError;
  }

  public MessageBuilder prepareForCreate(MessageBuilder messageBuilder) {
    messageBuilder
        .setContent(getContent())
        .addComponents(getButtons(false));
    for (File attachment : getAttachments()) {
      messageBuilder.addAttachment(attachment);
    }
    return messageBuilder;
  }

  public MessageUpdater prepareForEdit(MessageUpdater messageUpdater, boolean hasAnyReaction) {
    return messageUpdater
        .removeContent()
        .removeAllComponents()
        .setContent(getContent())
        .addComponents(getButtons(hasAnyReaction));
  }

  private String getContent() {
    StringBuilder sb = new StringBuilder();

    // Add raw !grats message.
    sb
        .append(String.format(RAW_GRATS_MESSAGE_FORMAT, eqLogEvent.getFullLine()))
        .append("\n");

    // Add loot command.
    if (lootCommandOrError.hasError()) {
      sb
          .append(String.format(LOOT_COMMAND_ERROR_FORMAT, lootCommandOrError.getError()))
          .append("\n");
    } else {
      sb
          .append(String.format(LOOT_COMMAND_SUCCESS_FORMAT, lootCommandOrError.getValue()))
          .append("\n");
    }

    // Add channel match.
    if (channelMatchOrError.hasError()) {
      sb
          .append(String.format(CHANNEL_MATCH_ERROR_FORMAT, channelMatchOrError.getError()))
          .append("\n");
    } else {
      sb
          .append(String.format(CHANNEL_MATCH_SUCCESS_FORMAT, channelMatchOrError.getValue()))
          .append("\n");
    }

    // Add item links.
    for (String itemUrl : itemUrls) {
      sb
          .append(itemUrl)
          .append("\n");
    }

    // Add item screenshot errors.
    for (ValueOrError<File> itemScreenshotOrError : itemScreenshotsOrErrors) {
      if (itemScreenshotOrError.hasError()) {
        sb
            .append(itemScreenshotOrError.getError())
            .append("\n");
      }
    }

    return sb.toString();
  }

  private ActionRow getButtons(boolean hasAnyReaction) {
    // Add "send to event channel" button.
    boolean hasError = lootCommandOrError.hasError() || channelMatchOrError.hasError();
    boolean enableButton = !hasError && !hasAnyReaction;
    return ActionRow.of(new ButtonBuilder()
        .setCustomId(DiscordButton.SEND_TO_EVENT_CHANNEL.getCustomId())
        .setStyle(enableButton ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY)
        .setDisabled(!enableButton)
        .setLabel("Send to event channel")
        .build());
  }

  private List<File> getAttachments() {
    List<File> attachments = Lists.newArrayList();

    // Add item screenshots.
    // Do this in reverse order so that they appear in the same order as the names.
    // Probably a Javacord bug.
    for (ValueOrError<File> itemScreenshotOrError : Lists.reverse(itemScreenshotsOrErrors)) {
      if (!itemScreenshotOrError.hasError()) {
        attachments.add(itemScreenshotOrError.getValue());
      }
    }

    return attachments;
  }

}
