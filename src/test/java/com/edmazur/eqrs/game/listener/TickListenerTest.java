package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.discord.Discord;
import com.edmazur.eqrs.discord.DiscordChannel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TickListenerTest {

  @Mock private Discord mockDiscord;

  private TickListener tickListener;

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
    tickListener = new TickListener(mockDiscord, new TickDetector());
  }

  @Test
  void tick() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'").get();
    tickListener.onEvent(eqLogEvent);
    verify(mockDiscord).sendMessage(any(DiscordChannel.class), eq(
        "🎟️ Possible tick sighting, ET: "
        + "`[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'`"));
  }

  @Test
  void tickWithContext() {
    EqLogEvent eqLogEvent1 = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'").get();
    EqLogEvent eqLogEvent2 = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:00:05 2022] Stanvern tells the guild, 'context in time'").get();
    EqLogEvent eqLogEvent3 = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:00:10 2022] Stanvern tells the guild, 'extra message'").get();
    tickListener.onEvent(eqLogEvent1);
    tickListener.onEvent(eqLogEvent2);
    tickListener.onEvent(eqLogEvent3);
    ArgumentCaptor<String> messagesCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockDiscord, times(2)).sendMessage(any(DiscordChannel.class), messagesCaptor.capture());
    List<String> messages = messagesCaptor.getAllValues();
    assertEquals(
        "🎟️ Possible tick sighting, ET: "
            + "`[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'`",
        messages.get(0));
    assertEquals(
        "⬆️ Possible tick context, ET: "
            + "`[Fri Jun 10 23:00:05 2022] Stanvern tells the guild, 'context in time'` "
            + "(tick-taker's next message within 15 seconds)",
        messages.get(1));
  }

  @Test
  void tickWithContextButTooLate() {
    EqLogEvent eqLogEvent1 = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'").get();
    EqLogEvent eqLogEvent2 = EqLogEvent.parseFromLine(
        "[Fri Jun 10 23:01:05 2022] Stanvern tells the guild, 'context not in time'").get();
    tickListener.onEvent(eqLogEvent1);
    tickListener.onEvent(eqLogEvent2);
    ArgumentCaptor<String> messagesCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockDiscord, times(1)).sendMessage(any(DiscordChannel.class), messagesCaptor.capture());
    List<String> messages = messagesCaptor.getAllValues();
    assertEquals(
        "🎟️ Possible tick sighting, ET: "
            + "`[Fri Jun 10 23:00:00 2022] Stanvern tells the guild, 'TICK'`",
        messages.get(0));
  }

}
