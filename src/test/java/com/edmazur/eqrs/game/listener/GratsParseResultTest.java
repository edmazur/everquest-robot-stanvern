package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.FakeMessageBuilder;
import com.edmazur.eqrs.ValueOrError;
import java.io.File;
import java.util.List;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.junit.jupiter.api.Test;

class GratsParseResultTest {

  private static final EqLogEvent EQ_LOG_EVENT = EqLogEvent.parseFromLine(
      "[Wed May 24 23:00:41 2023] Veriasse tells the guild, '!grats Resplendent Robe Veriasse 69'")
      .get();
  private static final List<String> ITEM_URLS =
      List.of("https://wiki.project1999.com/Resplendent_Robe");
  private static final long CHANNEL_ID = 123;
  private static final File FILE = new File("somefile");

  @Test
  void prepareForCreate() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        ITEM_URLS,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_apostropheBacktick() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EqLogEvent.parseFromLine(
            "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
                + "'!grats Chief Ry`Gorr's Head Veriasse 69'").get(),
        List.of("https://wiki.project1999.com/Chief_Ry%60Gorr%27s_Head"),
        ValueOrError.value("(loot command)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Chief Ry`Gorr's Head Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Chief_Ry%60Gorr%27s_Head\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_lootCommandError() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        ITEM_URLS,
        ValueOrError.error("(loot command error)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":x: **$loot command**: (loot command error)\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_channelMatchError() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        ITEM_URLS,
        ValueOrError.value("(loot command)"),
        ValueOrError.error("(channel match error)"),
        List.of(ValueOrError.value(FILE)));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":x: **Channel match**: (channel match error)\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
    assertEquals(FILE, fakeMessageBuilder.getAttachment());
  }

  @Test
  void prepareForCreate_missingScreenshot() {
    GratsParseResult gratsParseResult = new GratsParseResult(
        EQ_LOG_EVENT,
        ITEM_URLS,
        ValueOrError.value("(loot command)"),
        ValueOrError.value(CHANNEL_ID),
        List.of(ValueOrError.error("(item screenshot error)")));
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    String expected = ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
        + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n"
        + "(item screenshot error)\n";
    String actual = gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString();
    assertEquals(
        expected,
        actual);
    assertNull(fakeMessageBuilder.getAttachment());
  }

  @Test
  void fromMessage() {
    runFromMessageTest(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_apostropheBacktick() {
    runFromMessageTest(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Chief Ry`Gorr's Head Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Chief_Ry%60Gorr%27s_Head", true);
  }

  @Test
  void fromMessage_lootCommandError() {
    runFromMessageTest(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":x: **$loot command**: (loot command error)\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_channelMatchError() {
    runFromMessageTest(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":x: **Channel match**: (channel match error)\n"
        + "https://wiki.project1999.com/Resplendent_Robe", true);
  }

  @Test
  void fromMessage_missingScreenshot() {
    runFromMessageTest(
        ":moneybag: ET: ``[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'``\n"
        + ":white_check_mark: **$loot command**: ``(loot command)``\n"
        + ":white_check_mark: **Channel match**: <#123>\n"
        + "https://wiki.project1999.com/Resplendent_Robe\n"
        + "(item screenshot error)", false);
  }

  private void runFromMessageTest(String message, boolean hasAttachment) {
    Message mockMessage = mock(Message.class);
    when(mockMessage.getContent()).thenReturn(message);
    when(mockMessage.getAttachments())
        .thenReturn(hasAttachment ? List.of(mock(MessageAttachment.class)) : List.of());
    GratsParseResult gratsParseResult = GratsParseResult.fromMessage(mockMessage).get();
    FakeMessageBuilder fakeMessageBuilder = new FakeMessageBuilder();
    assertEquals(
        message + "\n",
        gratsParseResult.prepareForCreate(fakeMessageBuilder).getStringBuilder().toString());
  }

}
