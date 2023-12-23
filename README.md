# What is RobotStanvern?
RobotStanvern is a multi-purpose Discord bot for EverQuest, originally built for the \<Black Lotus>
guild on the Project 1999 green server and today run for \<Good Guys>. It can parse in-game logs,
manage ToDs, and perform a variety of Discord functions including sharing item screenshots,
batphoning earthquakes, and parsing guild chat for special commands.

# FAQ

## Will you run this for me for a P99 guild?

Unfortunately no, this is not something I'm currently offering outside of \<Good Guys> on P99 green.
Currently there's a lot of per-instance overhead that would require too much effort and ongoing
maintenance from me than I'm able to commit to (even for pay as some folks have generously offered).

I *may* one day in the future offer RobotStanvern as a service, but significant (and currently
unplanned) work will be required to make that a reality.

## What about outside of P99, e.g. Quarm?

Unfortunately no. All the above applies, as well as the additional complication of this bot not
having been tested at all outside of P99. It might work as is, or it might not (item dictionary
might need to be server-aware, log-reading might have server-specific nuances, etc.).

## Can I run it myself then?

Technically yes, but I *really* don't recommend it. You need *a lot* of motivation, a good amount of
technical knowledge, and probably a lot of hand-holding from me (which I may be slow or even unable
to provide).

Although *some* attempt has been made to parameterize various parts of the codebase, e.g. log
source, timezone, and database info, not everything is as portable as it ideally would be. Because
this project has always been focused on a specific guild's needs, there are still a lot of
guild-specific components that need to be generalized and parameterized (one example: Discord
channels). This is all solvable with effort (see above about one day potentially offering
RobotStanvern as a service), but would likely present significant challenges to all but the most
motivated (think hours and hours of work) and capable (likely someone practicing software
engineering for a living, but who knows!). In particular: The database schema is not in version
control (because I'm sloppy), nor is the logic for reading ToDs from the database and computing
windows (because of historical reasons).

## Do you take feature requests?

Yes! Feel free to contact me (see below). I have a pretty lengthy backlog and might not get to your
feature right away, but I do read everything and record notes about requests which I revisit when I
decide to spend time working on this codebase.

## Can I submit code to RobotStanvern?

Unfortunately no[\*], I don't have the bandwidth to do code reviews, and I'm selfishly too particular
about the codebase to not thoroughly review changes. This project started for me as a fun escape
from work where every decision is (rightfully) thoroughly evaluated and discussed, so it's been a
refreshing departure to be able to unilateraly do whatever I want without having to "answer" to
anyone or justify any subjective decisions.

[\*] The only exception *might* be super, super minor incremental changes that follow an existing
pattern. Please contact me (see below) to discuss first so you don't waste your time though.

## How can I contact you?

Discord is your best bet. I'm `edmazur` on there. You can DM me or ping me in the GG server if
you're in there too.

# Feature list

- Discord: Listens for reported ToDs and saves them to a database.
- Discord: Listens for messages in announcement/batphone channels and records them in a separate audit channel.
- Discord: Listens for screenshots of the char info part of the UI, then replies with a scrape summary of the name/class/level/exp.
- Discord: Listens for requests to show item info, then posts screenshots from the P99 wiki.
- Discord: Writes current/upcoming/later raid target windows to a channel.
- Game: Listens for FTE messages and sends a Discord message to a channel/user.
- Game: Listens for raid target spawns and sends a Discord message/screenshot to a channel/user and/or plays a sound on the local machine.
- Game: Detects inactivity and sends a Discord message to a user.
- Game: Listens for MotD messages and sends to Discord.
- Game: Listens for ToD reports in guild chat, sends to Discord, and tries to automatically record.
- Game: Listens for grats in guild chat and sends to Discord.
- Game: Listens for earthquakes and sends to Discord.

# Developer info

Setup:

```bash
$ git clone https://github.com/edmazur/everquest-robot-stanvern
$ cd everquest-robot-stanvern
$ cp app.config.EXAMPLE app.config
$ # (configure app.config)
```

Run in production:

```bash
$ git pull
$ ./gradlew installDist
$ ./build/install/everquest-robot-stanvern/bin/everquest-robot-stanvern YourCharacterName
```

Run in development mode (Discord interactions restricted to test server, database queries go to test
server):

```bash
$ ./src/main/scripts/override-test-db-with-prod-db.sh
$ ./gradlew run --args='YourCharacterName --debug'
```

Code coverage report (see `./build/reports/jacoco/test/html/index.html`):

```bash
$ ./gradlew build jacocoTestReport
```

TODO:
- Eliminate Runtime.exec() usage.
- Eliminate System.err/System.out logging.
