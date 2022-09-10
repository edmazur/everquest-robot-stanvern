package com.edmazur.eqrs.discord.listener;

import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.Config.Property;
import com.edmazur.eqrs.Logger;
import com.edmazur.eqrs.Pager;
import com.edmazur.eqrs.SoundPlayer;
import com.edmazur.eqrs.SoundPlayer.Sound;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.util.Arrays;
import java.util.List;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

// TODO: Use text-to-speech to say the batphone text on local machine.
// TODO: Use text-to-speech to say the batphone text in wake-up Discord channel.
// TODO: Integrate with some sort of dev ops tool.
public class BatphoneListener implements MessageCreateListener {

  private static final Logger LOGGER = new Logger();

  private static final List<DiscordChannel> CHANNELS_TO_READ_FROM = Arrays.asList(
      DiscordChannel.GG_BATPHONE);

  private final Config config;
  private final Discord discord;
  private final Pager pager;
  private final SoundPlayer soundPlayer;

  public BatphoneListener(Config config, Discord discord, Pager pager, SoundPlayer soundPlayer) {
    this.config = config;
    this.discord = discord;
    this.discord.addListener(this);
    this.pager = pager;
    this.soundPlayer = soundPlayer;
    LOGGER.log("%s running", this.getClass().getName());
  }

  @Override
  public void onMessageCreate(MessageCreateEvent event) {
    boolean isChannelToReadFrom = false;
    if (config.getBoolean(Property.DEBUG)) {
      isChannelToReadFrom = DiscordChannel.TEST_BATPHONE.isEventChannel(event);
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

    soundPlayer.play(Sound.ITS_TIME_TO_SLAY_THE_DRAGON);
    pager.page(event.getMessageContent());
  }

}
