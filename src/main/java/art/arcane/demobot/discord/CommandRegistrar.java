package art.arcane.demobot.discord;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.config.CommandRegistrationMode;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandRegistrar.class);

  private final BotConfig config;

  public CommandRegistrar(BotConfig config) {
    this.config = config;
  }

  public void register(JDA jda) {
    List<CommandData> commands = buildCommands();
    if (commands.isEmpty()) {
      LOGGER.warn("No commands enabled. Skipping slash command registration.");
      return;
    }

    if (config.bot().commandRegistrationMode() == CommandRegistrationMode.GUILD
        && config.bot().devGuildId() != null) {
      Guild guild = jda.getGuildById(config.bot().devGuildId());
      if (guild != null) {
        guild.updateCommands().addCommands(commands).queue();
        LOGGER.info("Registered {} commands in dev guild {}", commands.size(), guild.getId());
        return;
      }
      LOGGER.warn(
          "Configured dev guild {} not found, falling back to global command registration.",
          config.bot().devGuildId());
    }

    jda.updateCommands().addCommands(commands).queue();
    LOGGER.info("Registered {} commands globally.", commands.size());
  }

  private List<CommandData> buildCommands() {
    List<CommandData> commands = new ArrayList<>();

    if (config.features().pingEnabled()) {
      commands.add(Commands.slash("ping", "Check bot latency"));
    }

    if (config.features().linksEnabled()) {
      commands.add(Commands.slash("links", "View Arcane Arts documentation and support links"));
    }

    if (config.features().logGuideEnabled()) {
      commands.add(Commands.slash("logguide", "How to share useful logs and code for support"));
    }

    if (config.features().notifyEnabled()) {
      commands.add(Commands.slash("notify", "Choose notification topics"));
    }

    if (config.features().setupEnabled()) {
      commands.add(
          Commands.slash("setup", "Provision or audit managed server resources")
              .addSubcommands(
                  new SubcommandData("provision", "Create/repair managed roles and channels"),
                  new SubcommandData("audit", "Check managed resources without changing them")));
    }

    if (config.features().ticketEnabled()) {
      commands.add(
          Commands.slash("ticket", "Create and manage support tickets")
              .addSubcommands(
                  new SubcommandData("create", "Create a new support ticket"),
                  new SubcommandData("list", "List currently open tickets"),
                  new SubcommandData("claim", "Claim an open ticket")
                      .addOption(
                          OptionType.STRING,
                          "ticket_id",
                          "Ticket ID (optional when run in ticket channel)",
                          false),
                  new SubcommandData("panel", "Create/refresh ticket hub panel"),
                  new SubcommandData("setup", "Create/repair ticket hub resources"),
                  new SubcommandData("close", "Close a ticket")
                      .addOption(
                          OptionType.STRING,
                          "ticket_id",
                          "Ticket ID (optional when run in ticket channel)",
                          false),
                  new SubcommandData("transcript", "Download transcript for a closed ticket")
                      .addOption(OptionType.STRING, "ticket_id", "Ticket ID", true)));
    }

    return commands;
  }
}
