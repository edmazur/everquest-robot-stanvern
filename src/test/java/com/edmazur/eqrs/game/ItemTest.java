package com.edmazur.eqrs.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ItemTest {

  @Test
  void getNamePattern() {
    Item item = new Item("Swiftwind", "unused");

    assertFalse(item.getNamePattern().matcher("").matches());
    assertTrue(item.getNamePattern().matcher("Swiftwind").matches());
    assertFalse(item.getNamePattern().matcher("Earthcaller").matches());
    assertTrue(item.getNamePattern().matcher("Swiftwind rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Swiftwind").matches());
    assertTrue(item.getNamePattern().matcher("My Swiftwind rocks").matches());
    assertTrue(item.getNamePattern().matcher("My swiftwind rocks").matches());
    assertTrue(item.getNamePattern().matcher("My SWIFTWIND rocks").matches());
    assertTrue(item.getNamePattern().matcher("My SwIfTwInD rocks").matches());
  }

  @Test
  void getNamePattern_apostrophe() {
    Item item = new Item("Tserrina's Staff", "unused");

    assertTrue(item.getNamePattern().matcher("My Tserrina's Staff rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Tserrina`s Staff rocks").matches());
  }

  @Test
  void getNamePattern_backtick() {
    Item item = new Item("Abashi`s Rod of Disempowerment", "unused");

    assertTrue(item.getNamePattern().matcher("My Abashi`s Rod of Disempowerment rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Abashi's Rod of Disempowerment rocks").matches());
  }

  @Test
  void getNamePattern_both() {
    Item item = new Item("Chief Ry`Gorr's Head", "unused");

    assertTrue(item.getNamePattern().matcher("My Chief Ry`Gorr's Head rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Chief Ry`Gorr`s Head rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Chief Ry'Gorr's Head rocks").matches());
    assertTrue(item.getNamePattern().matcher("My Chief Ry'Gorr`s Head rocks").matches());
  }

}
