package com.edmazur.eqrs.game.listener;

import com.edmazur.eqlp.EqLogEvent;
import com.edmazur.eqrs.game.Item;
import com.edmazur.eqrs.game.ItemDatabase;
import com.edmazur.eqrs.game.ItemScreenshotter;
import java.io.File;
import java.util.List;
import java.util.Optional;

public class GratsParser {

  private final ItemDatabase itemDatabase;
  private final ItemScreenshotter itemScreenshotter;

  public GratsParser(
      ItemDatabase itemDatabase,
      ItemScreenshotter itemScreenshotter) {
    this.itemDatabase = itemDatabase;
    this.itemScreenshotter = itemScreenshotter;
  }

  public GratsParseResult parse(EqLogEvent eqLogEvent) {
    List<Item> items = itemDatabase.parse(eqLogEvent.getPayload());

    // TODO: Factor out the code that's repeated here and in ItemListener.
    GratsParseResult gratsParseResult = new GratsParseResult();
    gratsParseResult.addLine("💰 Possible !grats sighting, ET: `" + eqLogEvent.getFullLine() + "`");
    for (Item item : items) {
      gratsParseResult.addLine(item.getName() + " (" + item.getUrl() + ")");
    }
    // Add the attachments in reverse order so that they appear in the same order as the names.
    // Probably a Javacord bug.
    for (int i = items.size() - 1; i >= 0; i--) {
      Item item = items.get(i);
      Optional<File> maybeItemScreenshot = itemScreenshotter.get(item);
      if (maybeItemScreenshot.isPresent()) {
        gratsParseResult.addFile(maybeItemScreenshot.get());
      } else {
        gratsParseResult.addLine("(Error fetching screenshot for item: " + item.getName() + ")");
      }
    }

    return gratsParseResult;
  }

}
