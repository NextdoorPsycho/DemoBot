# Troubleshooting

## Bot fails at startup: missing token

- Ensure `config/bot.toml` has `[runtime].discord_token`.
- Ensure the token is valid and not expired.
- Or run with a Gradle override:
  - `./gradlew runBot -PdiscordToken=YOUR_TOKEN`
- Note: the bot now stays alive in wait mode and auto-retries when TOML is saved.

## Gradle fails before build with JVM version error

- This project requires Gradle to run on Java 21-23.
- If you see a Gradle startup failure mentioning unsupported JVM:
  - IntelliJ: set **Gradle JVM** to Java 21.
  - CLI: set `JAVA_HOME` to a Java 21 install before `./gradlew`.

## Commands not showing in Discord

- Confirm application has `applications.commands` scope.
- Check command registration mode in `config/bot.toml`.
- In `GLOBAL` mode, propagation may take several minutes.

## `/ticket create` says setup missing

- Run `/ticket setup` (or `/setup provision`) with admin privileges.

## Transcript not found

- Transcript is only generated when ticket closes successfully.
- Check `data/transcripts/<guildId>/`.

## Corrupt JSON state files

- Corrupt files are moved to `data/quarantine/` automatically.
- Re-run `/setup provision` after cleanup if resources drift.

## Config changes not applying

- Save your TOML files in `config/`.
- Hot-reload ignores invalid TOML and keeps the previous valid runtime.
- Changing `[runtime].discord_token` requires a process restart.
