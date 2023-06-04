package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordButton;
import com.edmazur.eqrs.discord.DiscordChannel;
import com.edmazur.eqrs.discord.DiscordRole;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.listener.GratsParseResult;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.event.message.reaction.ReactionEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveAllEvent;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.interaction.ButtonClickListener;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveAllListener;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;

public class GratsChannelListener implements
    ButtonClickListener, ReactionAddListener, ReactionRemoveAllListener, ReactionRemoveListener {

  private static final DiscordChannel PROD_CHANNEL = DiscordChannel.GG_TICKS_AND_GRATS;
  private static final DiscordChannel TEST_CHANNEL = DiscordChannel.TEST_GENERAL;

  private static final List<DiscordRole> PROD_ROLES = List.of(
      DiscordRole.GG_ADMIN,
      DiscordRole.GG_LEADER,
      DiscordRole.GG_OFFICER);
  private static final List<DiscordRole> TEST_ROLES = List.of(DiscordRole.TEST_ADMIN);

  private static final String SEND_NOTICE_PATTERN = "<@%d> sent $loot command from %s";

  private final Config config;
  private final Discord discord;
  private final ItemDatabase itemDatabase;

  public GratsChannelListener(Config config, Discord discord, ItemDatabase itemDatabase) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener((ButtonClickListener) this);
    this.discord.addListener((ReactionAddListener) this);
    this.discord.addListener((ReactionRemoveAllListener) this);
    this.discord.addListener((ReactionRemoveListener) this);
    this.itemDatabase = itemDatabase;
  }

  @Override
  public void onButtonClick(ButtonClickEvent event) {
    if (!isInRelevantChannel(event.getInteraction().getChannel().get())) {
      return;
    }

    if (!event.getButtonInteraction().getCustomId()
        .equals(DiscordButton.SEND_TO_EVENT_CHANNEL.getCustomId())) {
      return;
    }

    // Verify that the user has permission to do this.
    User user = event.getInteraction().getUser();
    if (!discord.hasAnyRole(user, getPermittedRoles(), event.getInteraction().getServer().get())) {
      user.sendMessage("You don't have one of the roles needed to use that button.");
      return;
    }

    // Reconstruct the GratsParseResult object.
    Message message = event.getInteraction().asMessageComponentInteraction().get().getMessage();
    Optional<GratsParseResult> maybeGratsParseResult =
        GratsParseResult.fromMessage(message, itemDatabase);
    if (maybeGratsParseResult.isEmpty()) {
      System.err.println(
          "Could not reconstruct GratsParseResult from message: " + message.getContent());
      return;
    }
    GratsParseResult gratsParseResult = maybeGratsParseResult.get();

    // Verify that the reconstructed GratsParseResult object has what we need.
    if (gratsParseResult.getLootCommandOrError().hasError()) {
      System.err.println("Button was clicked without loot command: " + message.getContent());
      return;
    }
    if (gratsParseResult.getChannelMatchOrError().hasError()) {
      System.err.println("Button was clicked channel match command: " + message.getContent());
      return;
    }

    // Send to event channel.
    String lootCommand = gratsParseResult.getLootCommandOrError().getValue();
    TextChannel channelMatch =
        discord.getTextChannel(gratsParseResult.getChannelMatchOrError().getValue());
    new MessageBuilder()
        .setAllowedMentions(new AllowedMentionsBuilder().build())
        .append(String.format(SEND_NOTICE_PATTERN, user.getId(), message.getLink().toString()))
        .send(channelMatch)
        .join();
    new MessageBuilder()
        .append(lootCommand)
        .send(channelMatch)
        .join();
    message.addReaction("👍");

    // Close out the button interaction.
    event.getInteraction().createImmediateResponder().respond();
  }

  @Override
  public void onReactionRemove(ReactionRemoveEvent event) {
    if (!isInRelevantChannel(event)) {
      return;
    }

    // Action is needed only if this event removed the last reaction.
    Message message = event.requestMessage().join();
    if (message.getReactions().isEmpty()) {
      handleHasAnyReactionChange(message, false);
    }
  }

  @Override
  public void onReactionRemoveAll(ReactionRemoveAllEvent event) {
    if (!isInRelevantChannel(event)) {
      return;
    }

    // Action is needed since, by definition, this event removed the last reaction.
    Message message = event.requestMessage().join();
    handleHasAnyReactionChange(message, false);
  }

  @Override
  public void onReactionAdd(ReactionAddEvent event) {
    if (!isInRelevantChannel(event)) {
      return;
    }

    // Action is needed only if this event added the first reaction.
    Message message = event.requestMessage().join();
    if (message.getReactions().size() == 1) {
      handleHasAnyReactionChange(message, true);
    }
  }

  private boolean isInRelevantChannel(ReactionEvent event) {
    return isInRelevantChannel(event.getChannel());
  }

  private boolean isInRelevantChannel(Channel channel) {
    return channel.getId() == getChannel().getId();
  }

  private void handleHasAnyReactionChange(Message message, boolean hasAnyReaction) {
    Optional<GratsParseResult> maybeGratsParseResult =
        GratsParseResult.fromMessage(message, itemDatabase);
    if (maybeGratsParseResult.isEmpty()) {
      System.err.println(
          "Could not reconstruct GratsParseResult from message: " + message.getContent());
      return;
    }
    GratsParseResult gratsParseResult = maybeGratsParseResult.get();
    gratsParseResult.prepareForEdit(message.createUpdater(), hasAnyReaction).applyChanges();
  }

  private DiscordChannel getChannel() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_CHANNEL;
    } else {
      return PROD_CHANNEL;
    }
  }

  private List<DiscordRole> getPermittedRoles() {
    if (config.getBoolean(Config.Property.DEBUG)) {
      return TEST_ROLES;
    } else {
      return PROD_ROLES;
    }
  }

}
