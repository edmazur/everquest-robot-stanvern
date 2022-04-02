# everquest-robot-stanvern
Multi-purpose Discord bot for EverQuest

# Usage

Setup:

```
$ git clone https://github.com/edmazur/everquest-robot-stanvern
$ cd everquest-robot-stanvern
$ cp app.config.EXAMPLE app.config
$ # (configure app.config)
```

Run:

```
$ git pull
$ ./gradlew installDist
$ ./build/install/everquest-robot-stanvern/bin/everquest-robot-stanvern
```

Debug mode: Append `--debug` on final Run command (Discord output only sent as DM, database writes skipped).

# Capabilities
- Discord: Listens for reported ToDs and saves them to a database.
- Discord: Listens for messages in announcement/batphone channels and records them in a separate audit channel.
- Discord: Listens for messages in batphone channels, then plays a sound on the local machine and sends an alert through PagerDuty.
- Game: Listens for FTE messages and sends a Discord message to a channel/user.
- Game: Listens for raid target spawns and sends a Discord message/screenshot to a channel/user and/or plays a sound on the local machine.
- Game: Detects inactivity and sends a Discord message to a user.
- Game: Listens for MotD messages and sends to Discord.
- Game: Listens for ToD reports in guild chat and sends to Discord.

# TODO
- Eliminate references to my local file system.
- Eliminate Runtime.exec() usage.
- Eliminate System.err/System.out logging.
- Use https://github.com/edmazur/everquest-log-parser instead of local copy of old game log parser.
- Add unit tests.
- Add a linter or something for trailing whitespace, EOF consistency, max line length, etc.