package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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
    assertSuccessfulParse("$loot Resplendent Robe Veriasse 69");
  }

  @Test
  void exclamationGratss() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon Jun 05 16:35:09 2023] Nightwyn tells the guild, "
        + "'!gratss Daewin Resplendent Wristguard 20'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Resplendent Wristguard Daewin 20");
  }

  @Test
  void lootStringDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0dkp'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Belt of Contention Darkace 0");
  }

  @Test
  void lootStringSpaceThenDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0 dkp'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Belt of Contention Darkace 0");
  }

  @Test
  void lootStringClosed() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 Closed Bigdumper'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Nature's Melody Bigdumper 650");
  }

  @Test
  void lootStringSwapping() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Jun 08 01:52:21 2023] Screeching tells the guild, "
        + "'!grats Klandicar's Talisman Raging 1000 swapping'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Klandicar's Talisman Raging 1000");
  }

  @Test
  void lootStringExtraWord() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 blah Bigdumper'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse(
        "``$loot Nature's Melody ??? 650`` "
            + "(Multiple name candidates found: ``blah``, ``Bigdumper``)");
  }

  @Test
  void lootStringExtraWords() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:14 2023] Britters tells the guild, "
        + "'!Grats Braid of Golden Hair Bobbydobby 333 (britters alt)'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse(
        "``$loot Braid of Golden Hair ??? 333`` "
            + "(Multiple name candidates found: ``Bobbydobby``, ``(britters``, ``alt)``)");
  }

  @Test
  void lootStringOutOfOrder() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 19:43:04 2023] Errur tells the guild, "
        + "'Wristband of the Bonecaster Sauromite 1 !grats'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Wristband of the Bonecaster Sauromite 1");
  }

  @Test
  void lootStringNoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon May 29 00:08:38 2023] Shlimm tells the guild, "
        + "'Orb of the Infinite Void 105 !grats'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("``$loot Orb of the Infinite Void ??? 105`` (No name found)");
  }

  @Test
  void lootStringTwoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 Hamfork / Guzmak'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse(
        "``$loot Spear of Fate ??? 400`` "
            + "(Multiple name candidates found: ``Hamfork``, ``Guzmak``)");
  }

  @Test
  void lootStringUppercaseName() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 HAMFORK'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Spear of Fate Hamfork 400");
  }

  @Test
  void lootStringBacktickItem() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun Jun 04 23:32:34 2023] Mccreary tells the guild, "
        + "'!grats Abashi's Rod of Disempowerment mccreary 3333'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Abashi's Rod of Disempowerment Mccreary 3333");
  }

  @Test
  void lootStringPunctuation() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Jun 15 21:55:21 2023] Trys tells the guild, "
        + "'!grats Spiroc Wingblade 1 Trys .'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Spiroc Wingblade Trys 1");
  }

  @Test
  void lootStringNameInParens() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Mon Aug 07 00:26:07 2023] Gnough tells the guild, "
        + "'!grats Vyemm's Right Eye 175  (sizar)'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("Name candidate contains non-alpha characters: ``(sizar)``");
  }

  @Test
  void lootStringPeriodAfterDkpAmount() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue Aug 08 22:46:11 2023] Ezzani tells the guild, "
        + "'!grats Amethyst Amulet Ezzani 1.'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Amethyst Amulet Ezzani 1");
  }

  private enum ParseOutcome {
    SUCCESS,
    FAILURE,
  }

  private void assertSuccessfulParse(String expectedOutput) {
    assertParse(expectedOutput, ParseOutcome.SUCCESS);
  }

  private void assertFailedParse(String expectedOutput) {
    assertParse(expectedOutput, ParseOutcome.FAILURE);
  }

  private void assertParse(String expectedOutput, ParseOutcome expectedParseOutcome) {
    switch (expectedParseOutcome) {
      case SUCCESS:
        if (lootCommandOrError.getValue() == null) {
          fail(String.format("Expected successful parse (%s), but it failed (%s)",
              expectedOutput, lootCommandOrError.getError()));
        } else {
          assertEquals(expectedOutput, lootCommandOrError.getValue());
        }
        break;
      case FAILURE:
        if (lootCommandOrError.getError() == null) {
          fail(String.format("Expected failed parse (%s), but it succeeded (%s)",
              expectedOutput, lootCommandOrError.getValue()));
        } else {
          assertEquals(expectedOutput, lootCommandOrError.getError());
        }
        break;
      default:
    }
  }

}
