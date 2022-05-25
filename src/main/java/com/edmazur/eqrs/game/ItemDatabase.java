package com.edmazur.eqrs.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemDatabase {

  private static final File ITEM_FILE =
      new File("/home/mazur/git/everquest-robot-stanvern/src/main/resources/items.txt");

  private Map<String, Item> itemsByName = new HashMap<>();

  public void initialize() {
    BufferedReader bufferedReader;
    try {
      bufferedReader = new BufferedReader(new FileReader(ITEM_FILE));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        String[] parts = line.split("\t");
        String name = parts[0];
        String url = parts[1];
        Item item = new Item(name, url);
        itemsByName.put(name, item);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      // TODO: Do something intelligent.
    } catch (IOException e) {
      e.printStackTrace();
      // TODO: Do something intelligent.
    }
  }

  public Optional<Item> getByName(String name) {
    Item item = itemsByName.get(name);
    if (item == null) {
      return Optional.empty();
    } else {
      return Optional.of(item);
    }
  }

}
