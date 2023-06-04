package com.edmazur.eqrs.discord;

public enum DiscordButton {

  SEND_TO_EVENT_CHANNEL("send-to-event-channel"),
  ;

  private final String customId;

  private DiscordButton(String customId) {
    this.customId = customId;
  }

  public String getCustomId() {
    return customId;
  }

}
