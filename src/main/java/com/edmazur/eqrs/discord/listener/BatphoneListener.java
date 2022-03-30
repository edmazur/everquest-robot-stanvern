package com.edmazur.eqrs.discord.listener;

import java.util.Arrays;
import java.util.List;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.Pager;
import com.edmazur.eqrs.Sound;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;

// TODO: Use text-to-speech to say the batphone text on local machine.
// TODO: Use text-to-speech to say the batphone text in wake-up Discord channel.
// TODO: Integrate with some sort of dev ops tool.
public class BatphoneListener implements MessageCreateListener {

  private static final Logger LOGGER = new Logger();

  private static final List<DiscordChannel> CHANNELS_TO_READ_FROM = Arrays.asList(
      DiscordChannel.RAID_BATPHONE,
      DiscordChannel.AFTERHOURS_BATPHONE);

  private final Config config;
  private final Discord discord;
  private final Pager pager;
  private final Sound sound;

  public BatphoneListener(
      Config config, Discord discord, Pager pager, Sound sound) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.pager = pager;
    this.sound = sound;
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    boolean isChannelToReadFrom = false;
    if (config.getBoolean(Property.DEBUG)) {
      isChannelToReadFrom =
          DiscordChannel.ROBOT_STANVERN_TESTING.isEventChannel(event);
    } else {
      for (DiscordChannel channelToReadFrom : CHANNELS_TO_READ_FROM) {
        if (channelToReadFrom.isEventChannel(event)) {
          isChannelToReadFrom = true;
          break;
        }
      }
    }
    if (!isChannelToReadFrom) {
      return;
    }

    sound.play();
    pager.page(event.getMessageContent());
  }

}