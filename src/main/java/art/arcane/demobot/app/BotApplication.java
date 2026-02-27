package art.arcane.demobot.app;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.config.ConfigException;
import art.arcane.demobot.config.ConfigLoader;
import art.arcane.demobot.config.ConfigValidator;
import art.arcane.demobot.discord.CommandRegistrar;
import art.arcane.demobot.discord.listener.BotInteractionListener;
import art.arcane.demobot.repository.GuildSettingsRepository;
import art.arcane.demobot.repository.TicketRepository;
import art.arcane.demobot.repository.UserPreferencesRepository;
import art.arcane.demobot.repository.json.JsonGuildSettingsRepository;
import art.arcane.demobot.repository.json.JsonTicketRepository;
import art.arcane.demobot.repository.json.JsonUserPreferencesRepository;
import art.arcane.demobot.service.EmbedFactory;
import art.arcane.demobot.service.NotifyService;
import art.arcane.demobot.service.PermissionService;
import art.arcane.demobot.service.SetupService;
import art.arcane.demobot.service.TicketService;
import art.arcane.demobot.util.ActivityParser;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BotApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(BotApplication.class);
  private static final String CONFIG_PATH_PROPERTY = "demobot.config";

  private BotApplication() {}

  public static void main(String[] args) throws InterruptedException {
    Path botTomlPath =
        Path.of(System.getProperty(CONFIG_PATH_PROPERTY, "config/bot.toml"))
            .toAbsolutePath()
            .normalize();

    BotCoordinator coordinator = new BotCoordinator(botTomlPath, new ConfigLoader());
    Runtime.getRuntime().addShutdownHook(new Thread(coordinator::shutdown, "bot-shutdown-hook"));

    coordinator.start();
    coordinator.blockUntilShutdown();
  }

  private static BotRuntimeComponents buildRuntimeComponents(BotConfig config) {
    ConfigValidator.validate(config);

    GuildSettingsRepository guildSettingsRepository =
        new JsonGuildSettingsRepository(
            config.runtime().dataDirPath(), config.storage().atomicWrites());
    UserPreferencesRepository userPreferencesRepository =
        new JsonUserPreferencesRepository(
            config.runtime().dataDirPath(), config.storage().atomicWrites());
    TicketRepository ticketRepository =
        new JsonTicketRepository(config.runtime().dataDirPath(), config.storage().atomicWrites());

    PermissionService permissionService = new PermissionService(config);
    EmbedFactory embedFactory = new EmbedFactory(config);
    SetupService setupService = new SetupService(config, guildSettingsRepository);
    NotifyService notifyService = new NotifyService(userPreferencesRepository);
    TicketService ticketService =
        new TicketService(
            config,
            ticketRepository,
            userPreferencesRepository,
            guildSettingsRepository,
            permissionService,
            embedFactory);

    return new BotRuntimeComponents(
        config, setupService, notifyService, ticketService, permissionService, embedFactory);
  }

  private static void printStartupError(String title, String message, List<String> actions) {
    String border = "=".repeat(98);
    System.err.println();
    System.err.println(border);
    System.err.println("ARCANE DEMOBOT STATUS: " + title);
    System.err.println(border);
    System.err.println("Reason:");
    System.err.println("  " + message);
    if (!actions.isEmpty()) {
      System.err.println();
      System.err.println("What to do:");
      for (int i = 0; i < actions.size(); i++) {
        System.err.println("  " + (i + 1) + ". " + actions.get(i));
      }
    }
    System.err.println(border);
    System.err.println();
  }

  private static final class BotCoordinator {
    private final Path botTomlPath;
    private final ConfigLoader loader;
    private final AtomicReference<BotRuntimeComponents> runtimeRef = new AtomicReference<>();
    private final Object lifecycleLock = new Object();

    private volatile boolean running;
    private volatile Thread watcherThread;
    private volatile JDA jda;
    private volatile String activeToken;
    private volatile String lastConfigError;
    private volatile boolean tokenMissingNoticeShown;

    private BotCoordinator(Path botTomlPath, ConfigLoader loader) {
      this.botTomlPath = botTomlPath;
      this.loader = loader;
    }

    public void start() {
      running = true;
      startWatcher();
      reload("startup");
    }

    public void blockUntilShutdown() throws InterruptedException {
      while (running) {
        Thread.sleep(1000L);
      }
    }

    public void shutdown() {
      running = false;
      if (watcherThread != null) {
        watcherThread.interrupt();
      }
      synchronized (lifecycleLock) {
        shutdownJdaLocked();
      }
    }

    private void startWatcher() {
      watcherThread = new Thread(this::watchLoop, "toml-hot-reload-watcher");
      watcherThread.setDaemon(true);
      watcherThread.start();
    }

    private void watchLoop() {
      Path configDir = botTomlPath.getParent();
      if (configDir == null) {
        printStartupError(
            "Configuration Directory Missing",
            "Could not resolve configuration directory from " + botTomlPath,
            List.of(
                "Ensure -PbotConfig points to a valid TOML file path.",
                "Then save the file again."));
        return;
      }

      try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
        Files.createDirectories(configDir);
        configDir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE);

        while (running && !Thread.currentThread().isInterrupted()) {
          WatchKey key = watchService.take();
          boolean tomlChanged = false;

          for (WatchEvent<?> event : key.pollEvents()) {
            Path changed = (Path) event.context();
            if (changed != null && changed.toString().toLowerCase().endsWith(".toml")) {
              tomlChanged = true;
            }
          }

          key.reset();

          if (!tomlChanged) {
            continue;
          }

          try {
            Thread.sleep(300L);
          } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            break;
          }

          reload("toml-change");
        }
      } catch (IOException exception) {
        LOGGER.error("TOML watcher stopped due to I/O failure", exception);
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      }
    }

    private void reload(String reason) {
      synchronized (lifecycleLock) {
        BotConfig config;
        try {
          config = loader.load(botTomlPath);
        } catch (ConfigException configException) {
          handleConfigException(configException);
          return;
        } catch (Exception exception) {
          LOGGER.error("Failed to load TOML config from {}", botTomlPath, exception);
          return;
        }

        tokenMissingNoticeShown = false;
        lastConfigError = null;

        try {
          Files.createDirectories(config.runtime().dataDirPath());
        } catch (IOException ioException) {
          LOGGER.error(
              "Failed to create data directory {}", config.runtime().dataDirPath(), ioException);
          return;
        }

        BotRuntimeComponents updated = buildRuntimeComponents(config);
        String newToken = updated.config().runtime().discordToken();

        if (jda == null) {
          startJdaLocked(updated, reason);
          return;
        }

        if (!newToken.equals(activeToken)) {
          LOGGER.info("Detected token change in TOML. Reconnecting bot session.");
          shutdownJdaLocked();
          startJdaLocked(updated, "token-change");
          return;
        }

        runtimeRef.set(updated);
        applyLiveConfigLocked(updated.config());
        LOGGER.info("Applied TOML hotload ({})", reason);
      }
    }

    private void handleConfigException(ConfigException configException) {
      String message =
          configException.getMessage() == null
              ? "Unknown configuration issue"
              : configException.getMessage();

      if (message.contains("runtime.discord_token is required")) {
        if (!tokenMissingNoticeShown) {
          printStartupError(
              "Waiting For Token",
              "No Discord token is configured yet. The bot will keep running and watch for TOML updates.",
              List.of(
                  "Set [runtime].discord_token in " + botTomlPath,
                  "Or run with override: ./gradlew runBot -PdiscordToken=YOUR_TOKEN",
                  "Save the TOML file. The bot will auto-connect without restart."));
          tokenMissingNoticeShown = true;
        }

        if (jda != null) {
          LOGGER.warn(
              "Config currently has no token. Keeping existing active Discord session until a valid token is provided.");
        }
        return;
      }

      if (!message.equals(lastConfigError)) {
        printStartupError(
            "Configuration Error",
            message,
            List.of(
                "Fix the value in " + botTomlPath,
                "Save the file. The bot will retry automatically."));
        lastConfigError = message;
      }

      if (jda != null) {
        LOGGER.warn("Ignoring invalid TOML update and keeping last known-good runtime.");
      }
    }

    private void startJdaLocked(BotRuntimeComponents updated, String reason) {
      runtimeRef.set(updated);
      BotInteractionListener interactionListener = new BotInteractionListener(runtimeRef::get);

      try {
        jda =
            JDABuilder.createDefault(updated.config().runtime().discordToken())
                .enableIntents(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setStatus(updated.config().bot().onlineStatus())
                .setActivity(ActivityParser.parse(updated.config().bot().activity()))
                .addEventListeners(interactionListener)
                .build()
                .awaitReady();

        new CommandRegistrar(updated.config()).register(jda);
        activeToken = updated.config().runtime().discordToken();

        LOGGER.info(
            "{} is online as {} (trigger: {})",
            updated.config().bot().appName(),
            jda.getSelfUser().getAsTag(),
            reason);
      } catch (InvalidTokenException invalidTokenException) {
        runtimeRef.set(null);
        jda = null;
        activeToken = null;
        printStartupError(
            "Invalid Discord Token",
            "Discord rejected the configured token. Waiting for a corrected token...",
            List.of(
                "Update [runtime].discord_token in " + botTomlPath,
                "Or run with: ./gradlew runBot -PdiscordToken=YOUR_TOKEN",
                "Save TOML. The bot will auto-retry connection."));
      } catch (Exception exception) {
        runtimeRef.set(null);
        jda = null;
        activeToken = null;
        LOGGER.error("Failed to start JDA", exception);
        printStartupError(
            "Discord Startup Error",
            exception.getClass().getSimpleName()
                + ": "
                + (exception.getMessage() == null ? "No message" : exception.getMessage()),
            List.of("Fix the issue and save TOML. The bot will retry on change."));
      }
    }

    private void applyLiveConfigLocked(BotConfig config) {
      if (jda == null) {
        return;
      }

      jda.getPresence().setStatus(config.bot().onlineStatus());
      jda.getPresence().setActivity(ActivityParser.parse(config.bot().activity()));
      new CommandRegistrar(config).register(jda);
    }

    private void shutdownJdaLocked() {
      if (jda == null) {
        return;
      }

      LOGGER.info("Shutting down JDA...");
      JDA localJda = jda;
      jda = null;
      activeToken = null;
      runtimeRef.set(null);

      localJda.shutdown();
      try {
        localJda.awaitShutdown(Duration.ofSeconds(15));
      } catch (InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
