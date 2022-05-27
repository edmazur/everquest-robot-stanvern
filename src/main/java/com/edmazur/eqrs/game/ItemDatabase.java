package com.edmazur.eqrs.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.ahocorasick.trie.PayloadTrie.PayloadTrieBuilder;

public class ItemDatabase {

  private static final File ITEM_FILE =
      new File("/home/mazur/git/everquest-robot-stanvern/src/main/resources/items.txt");

  private PayloadTrie<Item> itemsByName;

  public void initialize() {
    PayloadTrieBuilder<Item> itemsByNameBuilder = PayloadTrie.builder();
    itemsByNameBuilder.ignoreCase();
    BufferedReader bufferedReader;
    try {
      bufferedReader = new BufferedReader(new FileReader(ITEM_FILE));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        String[] parts = line.split("\t");
        String name = parts[0];
        String url = parts[1];
        Item item = new Item(name, url);
        itemsByNameBuilder.addKeyword(name, item);
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

  public List<Item> parse(String text) {
    List<Item> items = new ArrayList<>();
    for (PayloadEmit<Item> payload : itemsByName.parseText(text)) {
      items.add(payload.getPayload());
    }
    return items;
  }

}
