package com.edmazur.eqrs.game.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GratsParserTest {

  @Mock private ItemScreenshotter mockItemScreenshotter;

  private GratsParser gratsParser;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
    ItemDatabase itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
    gratsParser = new GratsParser(itemDatabase, mockItemScreenshotter);
  }

  @Test
  void grats() {
    EqLogEvent eqLogEvent = EqLogEvent.parseFromLine(
        "[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
        + "'!grats Resplendent Robe Veriasse 69'").get();
    GratsParseResult gratsParseResult = gratsParser.parse(eqLogEvent);

    assertEquals(3, gratsParseResult.getLines().size());
    assertEquals(
        "💰 Possible !grats sighting, ET: "
        + "`[Wed May 24 23:00:41 2023] Veriasse tells the guild, "
        + "'!grats Resplendent Robe Veriasse 69'`",
        gratsParseResult.getLines().get(0));
    assertEquals(
        "Resplendent Robe (https://wiki.project1999.com/Resplendent_Robe)",
        gratsParseResult.getLines().get(1));
    assertEquals(
        "(Error fetching screenshot for item: Resplendent Robe)",
        gratsParseResult.getLines().get(2));

    assertEquals(0, gratsParseResult.getFiles().size());
  }

}
