package com.edmazur.eqrs.discord;

import com.edmazur.eqrs.Logger;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import me.s3ns3iw00.jcommands.argument.ArgumentResult;
import me.s3ns3iw00.jcommands.event.listener.CommandActionEventListener;
import me.s3ns3iw00.jcommands.event.type.CommandActionEvent;
import me.s3ns3iw00.jcommands.type.ServerCommand;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public abstract class DiscordSlashCommand
    extends ServerCommand implements CommandActionEventListener {

  private static final Logger LOGGER = new Logger();
  private final List<DiscordSlashSubCommand> subCommands = new ArrayList<>();

  public DiscordSlashCommand(String name, String description) {
    super(name, description);
    setOnAction(this);

    // Discover subcommands
    Reflections reflections = new Reflections(
        "com.edmazur.eqrs.discord.commands." + name, Scanners.SubTypes);
    Set<Class<? extends DiscordSlashSubCommand>> subCommands = reflections.getSubTypesOf(
        DiscordSlashSubCommand.class);

    for (Class<? extends DiscordSlashSubCommand> subCommandClass : subCommands) {
      try {
        DiscordSlashSubCommand subCommand = subCommandClass.getDeclaredConstructor().newInstance();
        this.subCommands.add(subCommand);
        addArgument(subCommand);
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
               | InvocationTargetException e) {
        LOGGER.log("Error creating command: " + subCommandClass.getName());
      }
    }
  }

  @Override
  public void onAction(CommandActionEvent event) {
    ArgumentResult[] args = event.getArguments();

    String action = args[0].get();
    for (DiscordSlashSubCommand subCommand : subCommands) {
      if (action.equalsIgnoreCase(subCommand.getName())) {
        subCommand.onAction(event);
      }
    }
  }
}

