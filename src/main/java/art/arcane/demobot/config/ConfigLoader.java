package art.arcane.demobot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.dv8tion.jda.api.OnlineStatus;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;

public class ConfigLoader {
  private static final String DEFAULT_APP_NAME = "Arcane DemoBot";
  private static final String DISCORD_TOKEN_PROPERTY = "demobot.discordToken";
  private static final String OWNER_IDS_PROPERTY = "demobot.ownerIds";
  private static final String LOG_LEVEL_PROPERTY = "demobot.logLevel";
  private static final String DATA_DIR_PROPERTY = "demobot.dataDir";
  private static final String DEFAULT_TOML_TEMPLATE =
      """
      # Arcane DemoBot configuration
      # This file is hot-reloaded while the bot is running.
      # Save changes to apply them without restart (except Discord session token swaps may reconnect).

      [bot]
      # Display name used in startup logs and metadata.
      app_name = "Arcane DemoBot"
      # Activity format:
      #   watching:<text>
      #   playing:<text>
      #   listening:<text>
      #   competing:<text>
      activity = "watching:Support channels"
      # Allowed values: ONLINE, IDLE, DO_NOT_DISTURB, INVISIBLE
      online_status = "ONLINE"
      # Command registration strategy:
      #   GLOBAL -> register commands globally (can take time to propagate)
      #   GUILD  -> register commands only in dev_guild_id (fast for development)
      command_registration_mode = "GLOBAL"
      # Required only when command_registration_mode = "GUILD".
      # Use your Discord server ID.
      # dev_guild_id = 123456789012345678

      [branding]
      # Company/brand label shown in embeds and footer text.
      company_name = "Arcane Arts"
      # Primary embed color in hex (6 digits, with or without leading #).
      primary_color = "#0EA5E9"
      # Icon URL used in embed footers.
      avatar_url = "https://volmit.com/img/logo.png"
      # Public website URL.
      website_url = "https://volmit.com"
      # Discord invite/support URL.
      support_invite_url = "https://discord.gg/volmit"
      # Patreon URL (or any support URL).
      patreon_url = "https://www.patreon.com/volmitsoftware"
      # Product/documentation links shown by /links.
      adapt_docs_url = "https://docs.volmit.com/adapt/"
      react_docs_url = "https://docs.volmit.com/react/"
      iris_docs_url = "https://docs.volmit.com/iris/"
      holoui_docs_url = "https://docs.volmit.com/holoui/"

      [guild_defaults]
      # Default admin role name created/managed by /setup provision.
      admin_role_name = "Administrator"
      # Default support role name created/managed by /setup provision.
      support_role_name = "Support"
      # Suffix appended to notify role names (e.g. "Adapt - Notify").
      notify_role_suffix = " - Notify"
      # Ticket category/channel defaults used during setup.
      ticket_category_name = "Tickets"
      ticket_hub_channel_name = "ticket-hub"
      ticket_log_channel_name = "ticket-logs"

      [tickets]
      # Prefix for ticket channels (actual channel name is prefix + generated id).
      channel_prefix = "ticket-"
      # Minimum seconds between ticket create actions for the same user.
      creation_cooldown_seconds = 10
      # Maximum number of recent messages written into transcript on close.
      transcript_message_limit = 500
      # true  -> delete ticket channel when closed
      # false -> keep channel after close
      delete_channel_on_close = true
      # true  -> /setup provision posts/maintains ticket hub panel
      # false -> setup does not post the panel automatically
      post_hub_panel_on_provision = true

      [storage]
      # Base folder for bot runtime data.
      # Relative paths are resolved from the process working directory.
      data_dir = "./data"
      # true  -> write JSON via temp+move strategy (safer)
      # false -> direct writes
      atomic_writes = true

      [features]
      # Feature flags (true/false) for command modules.
      enable_ping = true
      enable_links = true
      enable_logguide = true
      enable_notify = true
      enable_setup = true
      enable_ticket = true

      [runtime]
      # Discord bot token from Discord Developer Portal.
      # Leave blank to keep bot in "waiting for token" mode.
      discord_token = ""
      # Owner user IDs with command bypass permissions.
      # Example: [123456789012345678, 987654321098765432]
      owner_ids = []
      # Logging level hint. Typical values: TRACE, DEBUG, INFO, WARN, ERROR.
      log_level = "INFO"
      """;

  public BotConfig load(Path configPath) {
    TomlParseResult result = parse(configPath);

    BotConfig.Bot bot =
        new BotConfig.Bot(
            stringValue(result, "bot.app_name", DEFAULT_APP_NAME),
            stringValue(result, "bot.activity", "Watching support channels"),
            parseOnlineStatus(stringValue(result, "bot.online_status", "ONLINE")),
            CommandRegistrationMode.fromValue(
                stringValue(result, "bot.command_registration_mode", "GLOBAL")),
            nullableLongValue(result, "bot.dev_guild_id"));

    BotConfig.Branding branding =
        new BotConfig.Branding(
            stringValue(result, "branding.company_name", "Arcane Arts"),
            stringValue(result, "branding.primary_color", "#0EA5E9"),
            stringValue(result, "branding.avatar_url", "https://volmit.com/img/logo.png"),
            stringValue(result, "branding.website_url", "https://volmit.com"),
            stringValue(result, "branding.support_invite_url", "https://discord.gg/volmit"),
            stringValue(result, "branding.patreon_url", "https://www.patreon.com/volmitsoftware"),
            stringValue(result, "branding.adapt_docs_url", "https://docs.volmit.com/adapt/"),
            stringValue(result, "branding.react_docs_url", "https://docs.volmit.com/react/"),
            stringValue(result, "branding.iris_docs_url", "https://docs.volmit.com/iris/"),
            stringValue(result, "branding.holoui_docs_url", "https://docs.volmit.com/holoui/"));

    BotConfig.GuildDefaults guildDefaults =
        new BotConfig.GuildDefaults(
            stringValue(result, "guild_defaults.admin_role_name", "Administrator"),
            stringValue(result, "guild_defaults.support_role_name", "Support"),
            stringValue(result, "guild_defaults.notify_role_suffix", " - Notify"),
            stringValue(result, "guild_defaults.ticket_category_name", "Tickets"),
            stringValue(result, "guild_defaults.ticket_hub_channel_name", "ticket-hub"),
            stringValue(result, "guild_defaults.ticket_log_channel_name", "ticket-logs"));

    BotConfig.Tickets tickets =
        new BotConfig.Tickets(
            stringValue(result, "tickets.channel_prefix", "ticket-"),
            intValue(result, "tickets.creation_cooldown_seconds", 10),
            intValue(result, "tickets.transcript_message_limit", 500),
            booleanValue(result, "tickets.delete_channel_on_close", true),
            booleanValue(result, "tickets.post_hub_panel_on_provision", true));

    BotConfig.Storage storage =
        new BotConfig.Storage(
            stringValue(result, "storage.data_dir", "./data"),
            booleanValue(result, "storage.atomic_writes", true));

    BotConfig.Features features =
        new BotConfig.Features(
            booleanValue(result, "features.enable_ping", true),
            booleanValue(result, "features.enable_links", true),
            booleanValue(result, "features.enable_logguide", true),
            booleanValue(result, "features.enable_notify", true),
            booleanValue(result, "features.enable_setup", true),
            booleanValue(result, "features.enable_ticket", true));

    String token =
        nonBlankOrFallback(
            System.getProperty(DISCORD_TOKEN_PROPERTY),
            stringValue(result, "runtime.discord_token", ""));
    Set<Long> ownerIds =
        parseOwnerIdsWithOverride(
            parseOwnerIds(result, "runtime.owner_ids"), System.getProperty(OWNER_IDS_PROPERTY));
    String logLevel =
        nonBlankOrFallback(
            System.getProperty(LOG_LEVEL_PROPERTY),
            stringValue(result, "runtime.log_level", "INFO"));
    String dataDir =
        nonBlankOrFallback(
            System.getProperty(DATA_DIR_PROPERTY),
            stringValue(result, "storage.data_dir", "./data"));

    BotConfig.Runtime runtime =
        new BotConfig.Runtime(
            token,
            ownerIds,
            logLevel.isBlank() ? "INFO" : logLevel,
            Path.of(dataDir).toAbsolutePath().normalize());

    BotConfig config =
        new BotConfig(bot, branding, guildDefaults, tickets, storage, features, runtime);
    ConfigValidator.validate(config);
    return config;
  }

  private TomlParseResult parse(Path configPath) {
    try {
      Path normalized = configPath.toAbsolutePath().normalize();
      ensureTemplateFiles(normalized);

      TomlParseResult result = Toml.parse(Files.newBufferedReader(normalized));
      if (result.hasErrors()) {
        throw new ConfigException("Invalid TOML config: " + result.errors());
      }
      return result;
    } catch (IOException e) {
      throw new ConfigException("Failed to read config file: " + configPath.toAbsolutePath(), e);
    }
  }

  private static void ensureTemplateFiles(Path configPath) throws IOException {
    Path parent = configPath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }

    if (!Files.exists(configPath)) {
      Files.writeString(
          configPath,
          DEFAULT_TOML_TEMPLATE,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW,
          StandardOpenOption.WRITE);
    }

    Path examplePath = configPath.resolveSibling(configPath.getFileName().toString() + ".example");
    if ("bot.toml".equalsIgnoreCase(configPath.getFileName().toString())) {
      examplePath = configPath.resolveSibling("bot.toml.example");
    }

    if (!Files.exists(examplePath)) {
      Files.writeString(
          examplePath,
          DEFAULT_TOML_TEMPLATE,
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW,
          StandardOpenOption.WRITE);
    }
  }

  private static String stringValue(TomlParseResult result, String key, String fallback) {
    String value = result.getString(key);
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private static int intValue(TomlParseResult result, String key, int fallback) {
    Long value = result.getLong(key);
    return value == null ? fallback : value.intValue();
  }

  private static boolean booleanValue(TomlParseResult result, String key, boolean fallback) {
    Boolean value = result.getBoolean(key);
    return value == null ? fallback : value;
  }

  private static Long nullableLongValue(TomlParseResult result, String key) {
    return result.getLong(key);
  }

  private static OnlineStatus parseOnlineStatus(String status) {
    try {
      return OnlineStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException ex) {
      return OnlineStatus.ONLINE;
    }
  }

  private static Set<Long> parseOwnerIds(TomlParseResult result, String key) {
    TomlArray values = result.getArray(key);
    if (values == null) {
      return Collections.emptySet();
    }

    Set<Long> ownerIds = new LinkedHashSet<>();
    for (int i = 0; i < values.size(); i++) {
      Object value = values.get(i);
      if (value instanceof Long longValue) {
        ownerIds.add(longValue);
        continue;
      }
      if (value instanceof String stringValue) {
        try {
          ownerIds.add(Long.parseLong(stringValue.trim()));
          continue;
        } catch (NumberFormatException ex) {
          throw new ConfigException("Invalid owner id in runtime.owner_ids: " + stringValue, ex);
        }
      }
      throw new ConfigException("runtime.owner_ids must contain only integers or numeric strings");
    }

    return Set.copyOf(ownerIds);
  }

  private static Set<Long> parseOwnerIdsWithOverride(Set<Long> fallback, String override) {
    if (override == null || override.isBlank()) {
      return fallback;
    }

    Set<Long> ownerIds = new LinkedHashSet<>();
    for (String token : override.split(",")) {
      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        continue;
      }
      try {
        ownerIds.add(Long.parseLong(trimmed));
      } catch (NumberFormatException ex) {
        throw new ConfigException("Invalid owner id in demobot.ownerIds: " + trimmed, ex);
      }
    }
    return Set.copyOf(ownerIds);
  }

  private static String nonBlankOrFallback(String primary, String fallback) {
    if (primary != null && !primary.isBlank()) {
      return primary.trim();
    }
    return fallback;
  }
}
