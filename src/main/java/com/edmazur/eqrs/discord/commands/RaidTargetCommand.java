package com.edmazur.eqrs.discord.commands;

import com.edmazur.eqrs.discord.DiscordSlashCommand;

/**
 * Raid Target Notifications!
 * Allow users to subscribe/unsubscribe from notifications for raid target windows.
 */
public class RaidTargetCommand extends DiscordSlashCommand {
  public RaidTargetCommand() {
    super("raidtarget", "Raid Target commands");
  }
}
