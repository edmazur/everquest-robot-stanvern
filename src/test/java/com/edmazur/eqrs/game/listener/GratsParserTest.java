package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

class GratsParserTest {

  private static ItemDatabase itemDatabase;

  @Mock private Config mockConfig;
  @Mock private ItemScreenshotter mockItemScreenshotter;
  @Mock private EventChannelMatcher mockEventChannelMatcher;

  private GratsParser gratsParser;

  private MockedConstruction<GratsParseResult> mockGratsParseResult;
  private ValueOrError<String> lootCommandOrError;

  @BeforeAll
  static void beforeAll() {
    itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.openMocks(this);

    gratsParser = new GratsParser(
        mockConfig,
        itemDatabase,
        mockItemScreenshotter,
        mockEventChannelMatcher);

    mockGratsParseResult = mockConstruction(GratsParseResult.class, (mock, context) -> {
      lootCommandOrError = (ValueOrError<String>) context.arguments().get(2);
    });
  }

  @AfterEach
  void afterEach() {
    mockGratsParseResult.close();
  }

  @Test
  void grats() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Resplendent Robe Veriasse 69", lootCommandOrError.getValue());
  }

  @Test
  void lootStringDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0dkp'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("Unrecognized input found: `0dkp`", lootCommandOrError.getError());
  }

  @Test
  void lootStringExtraWord() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 Closed Bigdumper'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "Multiple name candidates found: `Closed`, `Bigdumper`",
        lootCommandOrError.getError());
  }

  @Test
  void lootStringExtraWords() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:14 2023] Britters tells the guild, "
        + "'!Grats Braid of Golden Hair Bobbydobby 333 (britters alt)'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("Unrecognized input found: `(britters`, `alt)`", lootCommandOrError.getError());
  }

  @Test
  void lootStringOutOfOrder() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 19:43:04 2023] Errur tells the guild, "
        + "'Wristband of the Bonecaster Sauromite 1 !grats'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Wristband of the Bonecaster Sauromite 1", lootCommandOrError.getValue());
  }

  @Test
  void lootStringTwoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 Hamfork / Guzmak'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("Unrecognized input found: `/`", lootCommandOrError.getError());
  }

}
