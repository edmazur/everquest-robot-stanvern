package com.edmazur.eqrs.discord;

public enum DiscordRole {

  // Good Guys server.
  GG_ADMIN(1007091751518994432L),
  GG_COUNSEL(1012885582915043338L),
  GG_HR_AGENT(1007090816809967667L),
  GG_LEADER(1007090472210145342L),
  GG_OFFICER(1007090644805750805L),
  GG_QUARTERMASTER(1007090793107947630L),
  GG_RAID_DIRECTOR(1007091594941440181L),

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
