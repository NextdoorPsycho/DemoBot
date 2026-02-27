# Arcane DemoBot

Arcane DemoBot is a modern Java 21 Discord bot built on JDA 5 with slash-only commands, multi-guild support, TOML config, local JSON persistence, ticketing workflows, and CI quality gates.

## Highlights

- Slash-only command architecture (`/ping`, `/links`, `/logguide`, `/setup`, `/notify`, `/ticket`)
- Multi-guild settings with managed resources per guild
- Idempotent setup provisioning (safe, non-destructive)
- One-open-ticket-per-user policy with transcript export
- TOML-only configuration (`config/bot.toml`)
- Live TOML hot-reload on file change (`config/*.toml`)
- Java 21, Gradle 8, Spotless, Checkstyle, JaCoCo, JUnit 5
- Supports local JAR runs and Docker deployment

## Requirements

- Java 21
- Internet access for Discord + dependency download
- Discord bot token and application

## Quickstart (Local)

1. Copy config template:
   - `cp config/bot.toml.example config/bot.toml`
2. Set `[runtime].discord_token` in `config/bot.toml`.
3. Build and run:
   - `./gradlew clean shadowJar`
   - `java -jar build/libs/ArcaneDemoBot-2.0.0.jar`

Or use helper scripts:
- macOS/Linux: `./scripts/run-dev.sh`
- Windows: `scripts\\run-dev.bat`

### Gradle Run Profiles

- Standard run:
  - `./gradlew runBot`
- Run with token override (without editing TOML):
  - `./gradlew runBot -PdiscordToken=YOUR_TOKEN`
- Run with custom config path:
  - `./gradlew runBot -PbotConfig=/absolute/path/to/bot.toml`
- Debug profile (attach debugger on port 5005):
  - `./gradlew runBotDebug -PdiscordToken=YOUR_TOKEN`

### Startup Behavior

- If `runtime.discord_token` is missing or invalid, the bot no longer exits immediately.
- It shows a clear startup status screen and stays running in watch mode.
- Once you fix `config/bot.toml` and save, it auto-connects (no restart needed).

### Build Targets

- Build local copy target:
  - `./gradlew buildLocal`
  - Output: `build/targets/local/ArcaneDemoBot.jar`
- Build to any path:
  - `./gradlew buildTarget -PtargetDir=/absolute/path/to/plugins`
  - Optional custom file name:
    - `./gradlew buildTarget -PtargetDir=/path -PtargetName=DemoBot.jar`
- Named machine target (defined in `build.gradle`):
  - `./gradlew buildPsychoLT` (Unix)
  - `./gradlew buildPsycho` (Windows)

## Commands

- `/ping`
- `/links`
- `/logguide`
- `/setup provision`
- `/setup audit`
- `/notify`
- `/ticket create`
- `/ticket list`
- `/ticket claim [ticket_id]`
- `/ticket panel`
- `/ticket close [ticket_id]`
- `/ticket transcript <ticket_id>`
- `/ticket setup`

## Configuration

- Config file: `config/bot.toml`
  - Includes runtime values under `[runtime]`

See [docs/config.md](docs/config.md) for full details.

## Docker

1. Build image:
   - `docker compose build`
2. Run:
   - `docker compose up -d`

See [docs/deployment.md](docs/deployment.md).

## Quality

- Format check: `./gradlew spotlessCheck`
- Full validation: `./gradlew clean test check`

CI workflow runs these checks automatically on pushes/PRs.

## License

This project remains under GPLv3. See [LICENSE.md](LICENSE.md).
