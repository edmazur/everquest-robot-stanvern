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
$ ./build/install/everquest-robot-stanvern/bin/everquest-robot-stanvern YourCharacterName
```

Development (Discord interactions restricted to test server, database writes skipped):

```
$ ./gradlew run --args='YourCharacterName --debug'
```

# Capabilities
- Discord: Listens for reported ToDs and saves them to a database.
- Discord: Listens for messages in announcement/batphone channels and records them in a separate audit channel.
- Discord: Listens for messages in batphone channels, then sends an alert through PagerDuty.
- Discord: Listens for screenshots of the char info part of the UI, then replies with a scrape summary of the name/class/level/exp.
- Discord: Listens for requests to show item info, then posts screenshots from the P99 wiki.
- Discord: Writes current/upcoming/later raid target windows to a channel.
- Game: Listens for FTE messages and sends a Discord message to a channel/user.
- Game: Listens for raid target spawns and sends a Discord message/screenshot to a channel/user and/or plays a sound on the local machine.
- Game: Detects inactivity and sends a Discord message to a user.
- Game: Listens for MotD messages and sends to Discord.
- Game: Listens for ToD reports in guild chat, sends to Discord, and tries to automatically record.
- Game: Listens for ticks and their context in guild chat and sends to Discord.
- Game: Listens for grats in guild chat and sends to Discord.
- Game: Listens for earthquakes and sends to Discord.
- Game: Listens for tracking info and sends to Discord.

# TODO
- Eliminate Runtime.exec() usage.
- Eliminate System.err/System.out logging.
- Add unit tests.
