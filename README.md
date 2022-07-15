# DemoBot, The Discord Bot Template Master
The bots' repository is just a repo for a small server bot being that can do things. This is using Naming conventions
from [Volmit Software](https://github.com/VolmitSoftware). This bot is only showing off the JDA at the current time, and
making a bot to learn it. You are more than welcome to keep this code for whatever you want, as this is just a proof of
concept. AND it's a great starting place for JDA Bot Developments

## Getting Started
**This should help you get setup using this bot:**

- When you compile the bot, and execute the bot (as a jar. Use a `.bat` file.) Make this your config file
- You need to use `gradlew shadowJar` or you get a chonky jar file.

```json
{
  "botIMG": "",
  "botColor": "",
  "adminControllerRole": "",
  "xpPerMessage": {
    "min": 0.0,
    "max": 0.0
  },
  "discordID": "",
  "botCompany": "",
  "botPrefix": "",
  "xpBaseMultiplier": 0.0,
  "botOwnerID": "",
  "botToken": ""
}
```

### Prerequisites
- You need Gradle, Java 17, an internet connection, and some IDE (would suggest IntelliJ). Good luck with that)
- You need to have a Discord account, and have a bot account. (But you probably know that already)
- You need Manifold for the bot to work / to be able to code in the workspace (It's free, and it's a plugin, makes things so easy)
- THINGS YOU SHOULD INSTALL:
    - [Manifold](https://plugins.jetbrains.com/plugin/10057-manifold) It's for SUPER easy shorthands and clean code.
    - [Amulet](https://github.com/ArcaneArts/Amulet) This is an Addon by Cyberpwn to make working with projects easier, id suggest you take a look at it.
- THINGS YOU SHOULD FAMILIARIZE YOURSELF WITH:
    - [Gradle](https://gradle.org/) This is the build system for the bot.
    - [Java](https://www.java.com/) This is the language that the bot is written in.
    - [JDA](https://jda.dev/) This is the Discord API that the bot uses.
    - [Chewtils](https://github.com/Chew/JDA-Chewtils) This is a collection of useful utilities for the bot.
    - [Manifold](https://plugins.jetbrains.com/plugin/10057-manifold) Ez addon for working with projects.
    - [Amulet](https://github.com/ArcaneArts/Amulet) Ez addon for working with projects.
  
### Installing
- You can clone this repo and import the project in any Java IDE that you may want to use. This project was made using
  IntelliJ, but any should work as long as it supports gradle, and intel based plugins.
- Make sure that you have imported the plugins required via the Gradle file (look up how to do that for your own
  environment) and that your IDE recognize the folder `java` as Source folder and `resources` as Resources folder. If
  you have trouble getting the bot up and running, feel free
  to [create an issue](https://github.com/NextdoorPsycho/DemoBot/issues).

### Setting up your environment (Bot Token)
Since this is a self-hosted solution, you will need to provide your own Bot Token to get your bot up and running.
Here is a Bat file that i find helpful
```bat
@echo off
:start
java -jar DemoBot.jar 
goto start
```

If you don't know how to get your own bot token, you can
follow [this Github Tutorial](https://github.com/reactiflux/discord-irc/wiki/Creating-a-discord-bot-&-getting-a-token)
to create yourself one.

Once you have your bot token, you can run the program for the first time. Your `config` file should look
like this :

```json
{
  "botIMG": "",
  "botColor": "0x000000",
  "adminControllerRole": "Administrator",
  "xpPerMessage": {
    "min": 0.77500004,
    "max": 0.925
  },
  "discordID": "875973794504802334",
  "botSelfPersistence": "Watching for Messages!",
  "botStartupMessage": "Bot has Started",
  "botCompany": "VolmitSoftware",
  "botPrefix": ".",
  "xpBaseMultiplier": 2.13,
  "botOwnerID": "",
  "botToken": ""
}
```

The configuration file hot-loads, and when you make any changes it will inform you in the console that it updated your
settings.

### Config Breakdown
    "botIMG":
        The image that will be used for the bot's avatar.
    "botColor":
        The color of the bot's theme.
    "adminControllerRole":
        The role that is allowed to use the bot's admin commands.
    "xpPerMessage":
        The amount of xp that is given per message.
    "discordID":
        The ID of the bot.
    "botSelfPersistence":
        The message that the bot will say when its Actively Looking in chat
    "botStartupMessage":
        The message that the bot will say when it starts up.
    "botCompany":
        The company that the bot is made for. (you or whatever)
    "botPrefix":
        The prefix that the bot will use for prefix commands. (you can also use slash commands)
    "xpBaseMultiplier":
        The base multiplier for xp.
    "botOwnerID": 
        The ID of the bot owner.
    "botToken": 
        The token that the bot uses to connect to the discord api.

## Built With
- [JDA](https://github.com/DV8FromTheWorld/JDA) - The Java Discord APIs to allow a bot to run in Java
- [Gradle](https://gradle.org/) - Dependency Management

## Authors
- Brian Fopiano ([NextdoorPsycho](https://github.com/NextdoorPsycho)) - Creator and Owner of the software.
See also the list of [contributors](https://github.com/NextdoorPsycho/Abyssalith/contributors) who participated in this
project.

## License
This project is licensed under the GNU General Public License v3.0.
Opensource software is free to use, modify, and redistribute. So have fun!
Free Software Woo!
