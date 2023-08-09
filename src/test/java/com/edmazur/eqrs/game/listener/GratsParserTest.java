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
  void exclamationGrats() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Resplendent Robe Veriasse 69", lootCommandOrError.getValue());
  }

  @Test
  void exclamationGratss() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon Jun 05 16:35:09 2023] Nightwyn tells the guild, "
        + "'!gratss Daewin Resplendent Wristguard 20'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Resplendent Wristguard Daewin 20", lootCommandOrError.getValue());
  }

  @Test
  void lootStringDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0dkp'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Belt of Contention Darkace 0", lootCommandOrError.getValue());
  }

  @Test
  void lootStringSpaceThenDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0 dkp'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Belt of Contention Darkace 0", lootCommandOrError.getValue());
  }

  @Test
  void lootStringClosed() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 Closed Bigdumper'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Nature's Melody Bigdumper 650", lootCommandOrError.getValue());
  }

  @Test
  void lootStringSwapping() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Jun 08 01:52:21 2023] Screeching tells the guild, "
        + "'!grats Klandicar's Talisman Raging 1000 swapping'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Klandicar's Talisman Raging 1000", lootCommandOrError.getValue());
  }

  @Test
  void lootStringExtraWord() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 blah Bigdumper'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "``$loot Nature's Melody ??? 650`` "
            + "(Multiple name candidates found: ``blah``, ``Bigdumper``)",
        lootCommandOrError.getError());
  }

  @Test
  void lootStringExtraWords() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:14 2023] Britters tells the guild, "
        + "'!Grats Braid of Golden Hair Bobbydobby 333 (britters alt)'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "``$loot Braid of Golden Hair ??? 333`` "
            + "(Multiple name candidates found: ``Bobbydobby``, ``(britters``, ``alt)``)",
        lootCommandOrError.getError());
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
  void lootStringNoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon May 29 00:08:38 2023] Shlimm tells the guild, "
        + "'Orb of the Infinite Void 105 !grats'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "``$loot Orb of the Infinite Void ??? 105`` (No name found)",
        lootCommandOrError.getError());
  }

  @Test
  void lootStringTwoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 Hamfork / Guzmak'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "``$loot Spear of Fate ??? 400`` "
            + "(Multiple name candidates found: ``Hamfork``, ``Guzmak``)",
        lootCommandOrError.getError());
  }

  @Test
  void lootStringUppercaseName() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 HAMFORK'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Spear of Fate Hamfork 400", lootCommandOrError.getValue());
  }

  @Test
  void lootStringBacktickItem() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun Jun 04 23:32:34 2023] Mccreary tells the guild, "
        + "'!grats Abashi's Rod of Disempowerment mccreary 3333'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "$loot Abashi's Rod of Disempowerment Mccreary 3333",
        lootCommandOrError.getValue());
  }

  @Test
  void lootStringPunctuation() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Jun 15 21:55:21 2023] Trys tells the guild, "
        + "'!grats Spiroc Wingblade 1 Trys .'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Spiroc Wingblade Trys 1", lootCommandOrError.getValue());
  }

  @Test
  void lootStringNameInParens() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon Aug 07 00:26:07 2023] Gnough tells the guild, "
        + "'!grats Vyemm's Right Eye 175  (sizar)'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals(
        "Name candidate contains non-alpha characters: ``(sizar)``",
        lootCommandOrError.getError());
  }

  @Test
  void lootStringPeriodAfterDkpAmount() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue Aug 08 22:46:11 2023] Ezzani tells the guild, "
        + "'!grats Amethyst Amulet Ezzani 1.'").get();
    gratsParser.parse(eqLogEvent);
    assertEquals("$loot Amethyst Amulet Ezzani 1", lootCommandOrError.getValue());
  }

}
