package com.edmazur.eqrs.discord;

public enum DiscordCategory {

  // TBD server.
  TBD_PHONES(1007149113957171220L),
  TBD_IMPORTANT(1007150532919558266L),

  ;

  private final Long id;

  private DiscordCategory(long id) {
    this.id = id;
  }

  public long getId() {
    return id;
  }

}
