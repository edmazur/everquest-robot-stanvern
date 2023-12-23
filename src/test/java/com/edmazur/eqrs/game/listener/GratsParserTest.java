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
  void badInput() {
    assertFailedParse(
        "[Wed May 24 23:00:41 2023] Veriasse shouts, "
            + "'!grats Resplendent Robe Veriasse 69'",
        "Error reading guild chat");
  }

  @Test
  void exclamationGrats() {
    assertSuccessfulParse(
        "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
            + "'!grats Resplendent Robe Veriasse 69'",
        "$loot Resplendent Robe Veriasse 69");
  }

  @Test
  void exclamationGratss() {
    assertSuccessfulParse(
        "[Mon Jun 05 16:35:09 2023] Nightwyn tells the guild, "
            + "'!gratss Daewin Resplendent Wristguard 20'",
        "$loot Resplendent Wristguard Daewin 20");
  }

  @Test
  void lootStringDkpStringAfterNumber() {
    assertSuccessfulParse(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
            + "'!grats Darkace Belt of Contention 0dkp'",
        "$loot Belt of Contention Darkace 0");
  }

  @Test
  void lootStringSpaceThenDkpStringAfterNumber() {
    assertSuccessfulParse(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
            + "'!grats Darkace Belt of Contention 0 dkp'",
        "$loot Belt of Contention Darkace 0");
  }

  @Test
  void lootStringClosed() {
    assertSuccessfulParse(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
            + "'!Grats  Nature's Melody 650 Closed Bigdumper'",
        "$loot Nature's Melody Bigdumper 650");
  }

  @Test
  void lootStringSwapping() {
    assertSuccessfulParse(
        "[Thu Jun 08 01:52:21 2023] Screeching tells the guild, "
            + "'!grats Klandicar's Talisman Raging 1000 swapping'",
        "$loot Klandicar's Talisman Raging 1000");
  }

  @Test
  void lootStringExtraWord() {
    assertFailedParse(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
            + "'!Grats  Nature's Melody 650 blah Bigdumper'",
        "``$loot Nature's Melody ??? 650`` "
            + "(Multiple name candidates found: ``blah``, ``Bigdumper``)");
  }

  @Test
  void lootStringExtraWords() {
    assertFailedParse(
        "[Wed May 24 22:56:14 2023] Britters tells the guild, "
            + "'!Grats Braid of Golden Hair Bobbydobby 333 (britters alt)'",
        "``$loot Braid of Golden Hair ??? 333`` "
            + "(Multiple name candidates found: ``Bobbydobby``, ``(britters``, ``alt)``)");
  }

  @Test
  void lootStringOutOfOrder() {
    assertSuccessfulParse(
        "[Wed May 24 19:43:04 2023] Errur tells the guild, "
            + "'Wristband of the Bonecaster Sauromite 1 !grats'",
        "$loot Wristband of the Bonecaster Sauromite 1");
  }

  @Test
  void lootStringNoNames() {
    assertFailedParse(
        "[Mon May 29 00:08:38 2023] Shlimm tells the guild, "
            + "'Orb of the Infinite Void 105 !grats'",
        "``$loot Orb of the Infinite Void ??? 105`` (No name found)");
  }

  @Test
  void lootStringTwoNames() {
    assertFailedParse(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
            + "'!grats Spear of Fate 400 Hamfork / Guzmak'",
        "``$loot Spear of Fate ??? 400`` "
            + "(Multiple name candidates found: ``Hamfork``, ``Guzmak``)");
  }

  @Test
  void lootStringTwoNamesNoSpace() {
    assertFailedParse(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
            + "'!grats Spear of Fate 400 Hamfork/Guzmak'",
        "Name candidate contains non-alpha characters: ``Hamfork/Guzmak``");
  }

  @Test
  void lootStringUppercaseName() {
    assertSuccessfulParse(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
            + "'!grats Spear of Fate 400 HAMFORK'",
        "$loot Spear of Fate Hamfork 400");
  }

  @Test
  void lootStringBacktickItem() {
    assertSuccessfulParse(
        "[Sun Jun 04 23:32:34 2023] Mccreary tells the guild, "
            + "'!grats Abashi's Rod of Disempowerment mccreary 3333'",
        "$loot Abashi's Rod of Disempowerment Mccreary 3333");
  }

  @Test
  void lootStringPunctuation() {
    assertSuccessfulParse(
        "[Thu Jun 15 21:55:21 2023] Trys tells the guild, "
            + "'!grats Spiroc Wingblade 1 Trys .'",
        "$loot Spiroc Wingblade Trys 1");
  }

  @Test
  void lootStringNameInParens() {
    assertFailedParse(
        "[Mon Aug 07 00:26:07 2023] Gnough tells the guild, "
            + "'!grats Vyemm's Right Eye 175  (sizar)'",
        "Name candidate contains non-alpha characters: ``(sizar)``");
  }

  @Test
  void lootStringPeriodAfterDkpAmount() {
    assertSuccessfulParse(
        "[Tue Aug 08 22:46:11 2023] Ezzani tells the guild, "
            + "'!grats Amethyst Amulet Ezzani 1.'",
        "$loot Amethyst Amulet Ezzani 1");
  }

  @Test
  void lootStringSlashAfterDkpAmount() {
    assertSuccessfulParse(
        "[Thu Oct 05 17:09:48 2023] Dedale tells the guild, "
            + "'!grats Great Spear of Dawn Dedale 100/'",
        "$loot Great Spear of Dawn Dedale 100");
  }

  @Test
  void lootStringItemNameInAuthorName() {
    assertSuccessfulParse(
        "[Fri Sep 08 18:40:18 2023] Pebblespring tells the guild, "
            + "'!gratz Head of the Serpent Foo 0'",
        "$loot Head of the Serpent Foo 0");
  }

  @Test
  void lootStringItemNameInWinnerName() {
    assertSuccessfulParse(
        "[Tue Sep 26 04:29:21 2023] Etopia tells the guild, "
            + "'!gratz Spinning Orb of Confusion Pebblespring 50'",
        "$loot Spinning Orb of Confusion Pebblespring 50");
  }

  @Test
  void lootStringNoDkpAmount() {
    assertFailedParse(
        "[Thu Sep 07 17:00:02 2023] Ualine tells the guild, "
            + "'!grats Chestplate of Vindication ualine'",
        "No DKP amount found");
  }

  @Test
  void lootStringMulipleDkpAmounts() {
    assertFailedParse(
        "[Thu Oct 05 15:19:42 2023] Trys tells the guild, "
            + "'!grats Reaper's Ring 420 Trys / Britters 419 .'",
        "Multiple DKP amount candidates found: ``420``, ``419``");
  }

  @Test
  void lootStringNoItem() {
    assertFailedParse(
        "[Sun May 28 19:53:52 2023] Zalkestna tells the guild, "
            + "'!grats Zalkestna 475'",
        "No items found");
  }

  @Test
  void lootStringTypoItem() {
    assertFailedParse(
        "[Sun Jun 04 02:29:03 2023] Robomonk tells the guild, "
            + "'!grats Bow of the Hutsman Vill 12'",
        "No items found");
  }

  @Test
  void lootStringMultipleItems() {
    assertFailedParse(
        "[Wed May 24 23:00:41 2023] Stanvern tells the guild, "
            + "'!grats Swiftwind Earthcaller Stanvern 1337'",
        "Multiple items found");
  }

  private enum ParseOutcome {
    SUCCESS,
    FAILURE,
  }

  private void assertSuccessfulParse(String gameLogLine, String expectedOutput) {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(gameLogLine).get();
    gratsParser.parse(eqLogEvent);
    assertParse(expectedOutput, ParseOutcome.SUCCESS);
  }

  private void assertFailedParse(String gameLogLine, String expectedOutput) {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(gameLogLine).get();
    gratsParser.parse(eqLogEvent);
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
