# everquest-robot-stanvern
Multi-purpose Discord bot for EverQuest

# Capabilities
- Discord: Listens for reported ToDs and saves them to a database.
- Discord: Listens for messages in announcement/batphone channels and records them in a separate audit channel.
- Game: Listens for FTE messages and sends a Discord message to a channel/user and/or plays a sound on the local machine.
- Game: Listens for raid target spawns and sends a Discord message/screenshot to a channel/user and/or plays a sound on the local machine.
- Game: Detects inactivity and sends a Discord message to a user.
- Game: Listens for MotD messages and sends to Discord.

# TODO
- Make it feasible for other people to run this (build system? uber jar?).
- Document setup/running instructions.
- Eliminate references to my local file system.
- Eliminate Runtime.exec() usage.
- Eliminate System.err/System.out logging.
- Use https://github.com/edmazur/everquest-log-parser instead of local copy of old game log parser.
- Add unit tests.
- Add a linter or something for trailing whitespace, EOF consistency, max line length, etc.