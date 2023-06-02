package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.FakeMessageBuilder;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.discord.MessageBuilderFactory;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.javacord.api.entity.channel.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GratsParseResultTest {

  private static final EqLogEvent EQ_LOG_EVENT = EqLogEvent.parseFromLine(
      "[Wed May 24 23:00:41 2023] Veriasse tells the guild, '!grats Resplendent Robe Veriasse 69'")
      .get();
  private static final String ITEM_NAME = "Resplendent Robe";
  private static final long CHANNEL_ID = 123;
  private static final File FILE = new File("somefile");

  private List<Item> items;
  @Mock private Channel mockChannel;
  @Mock private ItemScreenshotter mockItemScreenshotter;

  private FakeMessageBuilder fakeMessageBuilder;
  @Mock private MessageBuilderFactory mockMessageBuilderFactory;

  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.openMocks(this);

    ItemDatabase itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
    items = itemDatabase.parse(ITEM_NAME);

    fakeMessageBuilder = new FakeMessageBuilder();
    when(mockMessageBuilderFactory.create()).thenReturn(fakeMessageBuilder);
    when(mockChannel.getId()).thenReturn(CHANNEL_ID);
  }

  @Test
  void getMessageBuilder() {
    when(mockItemScreenshotter.get(any(Item.class))).thenReturn(Optional.of(FILE));
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(mockChannel),
        mockMessageBuilderFactory);
    FakeMessageBuilder fakeMessageBuilder =
        (FakeMessageBuilder) gratsParseResult.getMessageBuilder(mockItemScreenshotter);
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        fakeMessageBuilder.getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void getMessageBuilder_lootCommandError() {
    when(mockItemScreenshotter.get(any(Item.class))).thenReturn(Optional.of(FILE));
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.error("(loot command error)"),
        ValueOrError.value(mockChannel),
        mockMessageBuilderFactory);
    FakeMessageBuilder fakeMessageBuilder =
        (FakeMessageBuilder) gratsParseResult.getMessageBuilder(mockItemScreenshotter);
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "❌ **$loot command**: (loot command error)\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        fakeMessageBuilder.getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void getMessageBuilder_channelMatchError() {
    when(mockItemScreenshotter.get(any(Item.class))).thenReturn(Optional.of(FILE));
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.error("(channel match error)"),
        mockMessageBuilderFactory);
    FakeMessageBuilder fakeMessageBuilder =
        (FakeMessageBuilder) gratsParseResult.getMessageBuilder(mockItemScreenshotter);
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "❌ **Channel match**: (channel match error)\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        fakeMessageBuilder.getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void getMessageBuilder_missingScreenshot() {
    when(mockItemScreenshotter.get(any(Item.class))).thenReturn(Optional.empty());
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        items,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(mockChannel),
        mockMessageBuilderFactory);
    FakeMessageBuilder fakeMessageBuilder =
        (FakeMessageBuilder) gratsParseResult.getMessageBuilder(mockItemScreenshotter);
    assertEquals(
        "💰 ET: `[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'`\n"
        + "✅ **$loot command**: `(loot command)`\n"
        + "✅ **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n"
        + "(Error fetching screenshot for item: Resplendent Robe)\n",
        fakeMessageBuilder.getStringBuilder().toString());
    assertNull(fakeMessageBuilder.getAttachment());
  }

}
