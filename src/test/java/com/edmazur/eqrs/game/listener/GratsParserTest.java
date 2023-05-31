package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.Config;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GratsParserTest {

  @Mock private Config mockConfig;
  @Mock private EventChannelMatcher mockEventChannelMatcher;
  @Mock private ItemScreenshotter mockItemScreenshotter;

  private GratsParser gratsParser;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    ItemDatabase itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
    gratsParser = new GratsParser(
        mockConfig, itemDatabase, mockEventChannelMatcher, mockItemScreenshotter);
  }

  @Test
  void grats() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
        + "'!grats Resplendent Robe Veriasse 69'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "💰 Possible !grats sighting, ET: "
        + "`[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
        + "'!grats Resplendent Robe Veriasse 69'`",
        gratsParseResult.getLines().get(0));
    assertEquals(
        "Resplendent Robe (https://wiki.project1999.com/Resplendent_Robe)",
        gratsParseResult.getLines().get(1));
    assertEquals(
        "✅ $loot parse succeeded: `$loot Resplendent Robe Veriasse 69`",
        gratsParseResult.getLines().get(2));
    assertEquals(
        "❌ Channel match failed: Item not found in any event channel's loot table",
        gratsParseResult.getLines().get(3));
    assertEquals(
        "(Error fetching screenshot for item: Resplendent Robe)",
        gratsParseResult.getLines().get(4));

    assertEquals(0, gratsParseResult.getFiles().size());
  }

  @Test
  void lootStringDkpStringAfterNumber() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Thu May 25 22:37:35 2023] Darkace tells the guild, "
        + "'!grats Darkace Belt of Contention 0dkp'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "❌ $loot parse failed: Unrecognized input found (0dkp)",
        gratsParseResult.getLines().get(2));
  }

  @Test
  void lootStringExtraWord() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:36 2023] Bigdumper tells the guild, "
        + "'!Grats  Nature's Melody 650 Closed Bigdumper'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "❌ $loot parse failed: Multiple name candidates found (closed, bigdumper)",
        gratsParseResult.getLines().get(2));
  }

  @Test
  void lootStringExtraWords() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 22:56:14 2023] Britters tells the guild, "
        + "'!Grats Braid of Golden Hair Bobbydobby 333 (britters alt)'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "❌ $loot parse failed: Unrecognized input found ((britters, alt))",
        gratsParseResult.getLines().get(2));
  }

  @Test
  void lootStringOutOfOrder() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 19:43:04 2023] Errur tells the guild, "
        + "'Wristband of the Bonecaster Sauromite 1 !grats'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "✅ $loot parse succeeded: `$loot Wristband of the Bonecaster Sauromite 1`",
        gratsParseResult.getLines().get(2));
  }

  @Test
  void lootStringTwoNames() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Tue May 23 17:31:38 2023] Faldimir tells the guild, "
        + "'!grats Spear of Fate 400 Hamfork / Guzmak'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(
        "❌ $loot parse failed: Unrecognized input found (/)",
        gratsParseResult.getLines().get(2));
  }

}
