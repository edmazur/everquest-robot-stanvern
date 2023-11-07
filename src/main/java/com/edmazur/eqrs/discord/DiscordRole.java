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
  GG_MEMBER(1007092100552196116L),

  // Test server.
  TEST_ADMIN(1114679989477060670L),
  TEST_MEMBER(693218141022715967L),

  // Automagical redirects
  ADMIN(-1),
  COUNSEL(-1),
  HR_AGENT(-1),
  LEADER(-1),
  OFFICER(-1),
  QUARTERMASTER(-1),
  RAID_DIRECTOR(-1),
  MEMBER(-1),

  ;

  private final Long id;

  private DiscordRole(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

  public DiscordRole getForServer(DiscordServer server) {
    // Determine which server we're using
    switch (server) {
      case GOOD_GUYS:
        switch (this) {
          case ADMIN:
            return GG_ADMIN;
          case COUNSEL:
            return GG_COUNSEL;
          case HR_AGENT:
            return GG_HR_AGENT;
          case LEADER:
            return GG_LEADER;
          case OFFICER:
            return GG_OFFICER;
          case QUARTERMASTER:
            return GG_QUARTERMASTER;
          case RAID_DIRECTOR:
            return GG_RAID_DIRECTOR;
          case MEMBER:
            return GG_MEMBER;
          default:
            return this;
        }
      case TEST:
        switch (this) {
          case ADMIN:
            return TEST_ADMIN;
          case MEMBER:
            return TEST_MEMBER;
          default:
            return this;
        }
      default:
        return this;
    }
  }
}
