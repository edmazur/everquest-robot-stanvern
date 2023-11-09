package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockConstruction;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.ValueOrError;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class GratsParserTest {

  private static MockedStatic<Config> mockConfig;
  private static MockedStatic<ItemScreenshotter> mockItemScreenshotter;
  private static MockedStatic<EventChannelMatcher> mockEventChannelMatcher;

  private GratsParser gratsParser;

  private MockedConstruction<GratsParseResult> mockGratsParseResult;
  private ValueOrError<String> lootCommandOrError;

  @BeforeAll
  static void beforeAll() {
    ItemDatabase.getItemDatabase(); // Initialize the Trie
    mockConfig = Mockito.mockStatic(Config.class);
    mockConfig.when(Config::getConfig).thenReturn(Mockito.mock(Config.class));
    mockItemScreenshotter = Mockito.mockStatic(ItemScreenshotter.class);
    mockEventChannelMatcher = Mockito.mockStatic(EventChannelMatcher.class);
  }

  @AfterAll
  static void afterAll() {
    mockConfig.close();
    mockItemScreenshotter.close();
    mockEventChannelMatcher.close();
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void beforeEach() {
    MockitoAnnotations.openMocks(this);

    gratsParser = new GratsParser();

    mockGratsParseResult = mockConstruction(GratsParseResult.class, (mock, context) -> {
      lootCommandOrError = (ValueOrError<String>) context.arguments().get(2);
    });
  }

  @AfterEach
  void afterEach() {
    mockGratsParseResult.close();
    try {
      MockitoAnnotations.openMocks(this).close();
    } catch (Exception e) {
      // This is fine
    }
  }

  @Test
  void badInput() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Veriasse shouts, "
            + "'!grats Resplendent Robe Veriasse 69'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("Error reading guild chat");
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
  void lootStringTwoNamesNoSpace() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 Hamfork/Guzmak'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("Name candidate contains non-alpha characters: ``Hamfork/Guzmak``");
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

  @Test
  void lootStringSlashAfterDkpAmount() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Oct 05 17:09:48 2023] Dedale tells the guild, "
        + "'!grats Great Spear of Dawn Dedale 100/'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Great Spear of Dawn Dedale 100");
  }

  @Test
  void lootStringItemNameInAuthorName() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Fri Sep 08 18:40:18 2023] Pebblespring tells the guild, "
        + "'!gratz Head of the Serpent Foo 0'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Head of the Serpent Foo 0");
  }

  @Test
  void lootStringItemNameInWinnerName() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue Sep 26 04:29:21 2023] Etopia tells the guild, "
        + "'!gratz Spinning Orb of Confusion Pebblespring 50'").get();
    gratsParser.parse(eqLogEvent);
    assertSuccessfulParse("$loot Spinning Orb of Confusion Pebblespring 50");
  }

  @Test
  void lootStringNoDkpAmount() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Sep 07 17:00:02 2023] Ualine tells the guild, "
        + "'!grats Chestplate of Vindication ualine'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("No DKP amount found");
  }

  @Test
  void lootStringMulipleDkpAmounts() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu Oct 05 15:19:42 2023] Trys tells the guild, "
        + "'!grats Reaper's Ring 420 Trys / Britters 419 .'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("Multiple DKP amount candidates found: ``420``, ``419``");
  }

  @Test
  void lootStringNoItem() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun May 28 19:53:52 2023] Zalkestna tells the guild, '!grats Zalkestna 475'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("No items found");
  }

  @Test
  void lootStringTypoItem() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Sun Jun 04 02:29:03 2023] Robomonk tells the guild, "
        + "'!grats Bow of the Hutsman Vill 12'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("No items found");
  }

  @Test
  void lootStringMultipleItems() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Stanvern tells the guild, "
            + "'!grats Swiftwind Earthcaller Stanvern 1337'").get();
    gratsParser.parse(eqLogEvent);
    assertFailedParse("Multiple items found");
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
