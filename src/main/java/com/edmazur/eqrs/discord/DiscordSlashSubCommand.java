package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.Logger;
import me.s3ns3iw00.jcommands.argument.type.ConstantArgument;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;

public abstract class DiscordSlashSubCommand extends ConstantArgument {
  protected static final Logger LOGGER = new Logger();

  public DiscordSlashSubCommand(String name, String description) {
    super(name, description);
    LOGGER.log("%s running", this.getClass().getName());
  }

  public abstract void onAction(CommandActionEvent event);
}
