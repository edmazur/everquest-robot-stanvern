package com.edmazur.eqrs.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;

public class ItemDatabase {

  private static final File ITEM_FILE =
      new File("/home/mazur/git/everquest-robot-stanvern/src/main/resources/items.txt");
  private static final File SPAMMY_ITEM_FILE =
      new File("/home/mazur/git/everquest-robot-stanvern/src/main/resources/spammy-items.txt");

  private PayloadTrie<Item> itemsByName;
  private Set<String> spammyItems;

  public void initialize() {
    PayloadTrieBuilder<Item> itemsByNameBuilder = PayloadTrie.builder();
    itemsByNameBuilder
        .ignoreCase()
        .ignoreOverlaps();
    spammyItems = getSpammyItems();
    BufferedReader bufferedReader;
    try {
      bufferedReader = new BufferedReader(new FileReader(ITEM_FILE));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        String[] parts = line.split("\t");
        String name = parts[0];
        String url = parts[1];
        Item item = new Item(name, url);
        itemsByNameBuilder.addKeyword(normalize(name), item);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      // TODO: Do something intelligent.
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: Do something intelligent.
    }
    itemsByName = itemsByNameBuilder.build();
  }

  private Set<String> getSpammyItems() {
    Set<String> spammyItems = null;
    try {
      spammyItems = new HashSet<String>(Files.readAllLines(SPAMMY_ITEM_FILE.toPath()));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      // TODO: Do something intelligent.
    }
    return spammyItems;
  }

  public List<Item> parse(String text) {
    List<Item> items = new ArrayList<>();
    Set<Item> seenSpammyItems = new HashSet<>();
    for (PayloadEmit<Item> payload : itemsByName.parseText(normalize(text))) {
      Item item = payload.getPayload();
      items.add(item);
      if (spammyItems.contains(item.getName())) {
        seenSpammyItems.add(item);
      }
    }
    if (seenSpammyItems.isEmpty()) {
      return items;
    } else {
      // Return spammy items if and only if they are the only item on the list.
      if (items.size() > 1) {
        items.removeIf(item -> seenSpammyItems.contains(item));
      }
      return items;
    }
  }

  // Make apostrophes and backticks interchangeable:
  // - Makes it easier for one-off manual lookups.
  // - NinjaLooter replaces backticks with apostrophes.
  private String normalize(String s) {
    return s.replace("`", "'");
  }

}
