package com.edmazur.eqrs.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ItemDatabaseTest {

  private static ItemDatabase itemDatabase;

  @BeforeAll
  static void init() {
    itemDatabase = new ItemDatabase();
    itemDatabase.initialize();
  }

  @Test
  void getSingleItem() {
    List<Item> items = itemDatabase.parse("Swiftwind");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getSingleItemLower() {
    List<Item> items = itemDatabase.parse("swiftwind");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getSingleItemUpper() {
    List<Item> items = itemDatabase.parse("SWIFTWIND");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getSingleItemPrefix() {
    List<Item> items = itemDatabase.parse("Swiftwind is the best");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getSingleItemSuffix() {
    List<Item> items = itemDatabase.parse("I love Swiftwind");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getSingleItemMidfix() {
    List<Item> items = itemDatabase.parse("I love Swiftwind it is the best");
    assertEquals(1, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
  }

  @Test
  void getMultipleItems() {
    List<Item> items = itemDatabase.parse("I love Swiftwind and Earthcaller they are the best");
    assertEquals(2, items.size());
    assertEquals("Swiftwind", items.get(0).getName());
    assertEquals("Earthcaller", items.get(1).getName());
  }

  @Test
  void getItemWithSubstringItems() {
    List<Item> items = itemDatabase.parse("Black Sapphire Electrum Earring");
    assertEquals(1, items.size());
    assertEquals("Black Sapphire Electrum Earring", items.get(0).getName());
  }

  @Test
  void getItemWithApostrophe() {
    List<Item> items = itemDatabase.parse("Zlandicar's Talisman");
    assertEquals(1, items.size());
    assertEquals("Zlandicar's Talisman", items.get(0).getName());
  }

  @Test
  void getItemWithBacktick() {
    List<Item> items = itemDatabase.parse("Palladius` Axe of Slaughter");
    assertEquals(1, items.size());
    assertEquals("Palladius` Axe of Slaughter", items.get(0).getName());
  }

  @Test
  void getItemBacktickInputForApostropheItem() {
    List<Item> items = itemDatabase.parse("Zlandicar`s Talisman");
    assertEquals(1, items.size());
    assertEquals("Zlandicar's Talisman", items.get(0).getName());
  }

  @Test
  void getItemApostropheInputForBacktickItem() {
    List<Item> items = itemDatabase.parse("Palladius' Axe of Slaughter");
    assertEquals(1, items.size());
    assertEquals("Palladius` Axe of Slaughter", items.get(0).getName());
  }

}
