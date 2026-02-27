# Deployment

## Local JAR

1. Create `config/bot.toml` from the example.
2. Set `[runtime].discord_token`.
3. Build:
   - `./gradlew clean shadowJar`
4. Run:
   - `java -jar build/libs/ArcaneDemoBot-2.0.0.jar`

## Docker Compose

1. Create `config/bot.toml`.
2. Set `[runtime].discord_token`.
3. Build and start:
   - `docker compose up -d --build`
4. View logs:
   - `docker compose logs -f`
5. Stop:
   - `docker compose down`

## Command Registration Modes

- `GLOBAL`: commands are registered globally (can take time to propagate).
- `GUILD`: commands are registered only to `bot.dev_guild_id` for rapid iteration.

## Volumes

- `./data` mounted to `/app/data`
- `./config` mounted to `/app/config`
