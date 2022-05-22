package com.edmazur.eqrs.game;

public class CharInfo {

  private String name;
  private EqClass eqClass;
  private Integer level;
  private Integer expPercentToNextLevel; // [0-99]

  public CharInfo setName(String name) {
    this.name = name;
    return this;
  }

  public boolean hasName() {
    return name != null;
  }

  public String getName() {
    return name;
  }

  public CharInfo setEqClass(EqClass eqClass) {
    this.eqClass = eqClass;
    return this;
  }

  public boolean hasEqClass() {
    return eqClass != null;
  }

  public EqClass getEqClass() {
    return eqClass;
  }

  public CharInfo setLevel(int level) {
    this.level = level;
    return this;
  }

  public boolean hasLevel() {
    return level != null;
  }

  public Integer getLevel() {
    return level;
  }

  public CharInfo setExpPercentToNextLevel(int expPercentToNextLevel) {
    this.expPercentToNextLevel = expPercentToNextLevel;
    return this;
  }

  public boolean hasExpPercentToNextLevel() {
    return expPercentToNextLevel != null;
  }

  public Integer getExpPercentToNextLevel() {
    return expPercentToNextLevel;
  }

  @Override
  public String toString() {
    return String.format("name=%s, eqClass=%s, level=%d, expPercentToNextLevel=%d",
        name, eqClass, level, expPercentToNextLevel);
  }

}
