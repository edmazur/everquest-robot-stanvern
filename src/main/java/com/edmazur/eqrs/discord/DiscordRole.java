package com.edmazur.eqrs.discord;

public enum DiscordRole {

  // Good Guys server.
  GG_ADMIN(1007091751518994432L),
  GG_LEADER(1007090472210145342L),
  GG_OFFICER(1007090644805750805L),

  // Test server.
  TEST_ADMIN(1114679989477060670L),

  ;

  private final Long id;

  private DiscordRole(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

}
