# Configuration

Arcane DemoBot uses TOML-only configuration.

- Main config: `config/bot.toml`
- Template: `config/bot.toml.example`

The app hot-reloads TOML changes when files under `config/` are modified.

## TOML Sections

### `[bot]`
- `app_name`
- `activity` (`watching:...`, `playing:...`, `listening:...`, `competing:...`)
- `online_status` (`ONLINE`, `IDLE`, `DO_NOT_DISTURB`, `INVISIBLE`)
- `command_registration_mode` (`GLOBAL` or `GUILD`)
- `dev_guild_id` (required only when using `GUILD` mode)

### `[branding]`
Branding values used in embeds and link commands.

### `[guild_defaults]`
Default names for admin/support roles and ticket resources.

### `[tickets]`
Ticket behavior and transcript settings.

### `[storage]`
Persistent data location and atomic write mode.

### `[features]`
Command-level feature flags.

### `[runtime]`
- `discord_token` (required)
- `owner_ids` (optional array of Discord user IDs)
- `log_level` (optional)

## Data Layout

Runtime JSON data is stored under `data/` (or `storage.data_dir`).

- `data/guilds/<guildId>/settings.json`
- `data/users/<guildId>/<userId>.json`
- `data/tickets/<guildId>/<ticketId>.json`
- `data/transcripts/<guildId>/<ticketId>.md`
- `data/quarantine/` for corrupted JSON files
