package art.arcane.demobot.discord.listener;

import art.arcane.demobot.app.BotRuntimeComponents;
import art.arcane.demobot.model.GuildSettings;
import art.arcane.demobot.model.UserPreferences;
import art.arcane.demobot.service.NotifyService;
import art.arcane.demobot.service.SetupAuditResult;
import art.arcane.demobot.service.SetupProvisionResult;
import art.arcane.demobot.service.TicketClaimResult;
import art.arcane.demobot.service.TicketCloseResult;
import art.arcane.demobot.service.TicketCreateResult;
import art.arcane.demobot.service.TicketService;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;

public class BotInteractionListener extends ListenerAdapter {
  private final Supplier<BotRuntimeComponents> runtime;

  public BotInteractionListener(Supplier<BotRuntimeComponents> runtime) {
    this.runtime = runtime;
  }

  @Override
  public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
    switch (event.getName()) {
      case "ping" -> handlePing(event);
      case "links" -> handleLinks(event);
      case "logguide" -> handleLogGuide(event);
      case "setup" -> handleSetup(event);
      case "notify" -> handleNotify(event);
      case "ticket" -> handleTicket(event);
      default -> event.reply("Unknown command.").setEphemeral(true).queue();
    }
  }

  @Override
  public void onStringSelectInteraction(StringSelectInteractionEvent event) {
    if (!NotifyService.NOTIFY_SELECT_ID.equals(event.getComponentId())) {
      return;
    }
    if (event.getGuild() == null || event.getMember() == null) {
      event.reply("This interaction can only be used inside a server.").setEphemeral(true).queue();
      return;
    }

    BotRuntimeComponents components = runtime.get();
    GuildSettings settings =
        components.setupService().getOrCreateSettings(event.getGuild().getIdLong());
    List<String> changes =
        components.notifyService().applySelections(event.getMember(), settings, event.getValues());
    event.reply(String.join("\n", changes)).setEphemeral(true).queue();
  }

  @Override
  public void onButtonInteraction(ButtonInteractionEvent event) {
    if (event.getGuild() == null || event.getMember() == null) {
      event.reply("This interaction can only be used inside a server.").setEphemeral(true).queue();
      return;
    }

    BotRuntimeComponents components = runtime.get();
    GuildSettings settings =
        components.setupService().getOrCreateSettings(event.getGuild().getIdLong());

    if (TicketService.CREATE_TICKET_BUTTON_ID.equals(event.getComponentId())) {
      TicketCreateResult result =
          components.ticketService().createTicket(event.getMember(), settings);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if (TicketService.OPEN_MY_TICKET_BUTTON_ID.equals(event.getComponentId())) {
      TicketCreateResult result =
          components.ticketService().openOrCreateTicket(event.getMember(), settings);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if (TicketService.CLAIM_TICKET_BUTTON_ID.equals(event.getComponentId())) {
      TextChannel textChannel = event.getChannel().asTextChannel();
      TicketClaimResult result =
          components.ticketService().claimTicket(event.getMember(), settings, null, textChannel);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if (TicketService.CLOSE_TICKET_BUTTON_ID.equals(event.getComponentId())) {
      TextChannel textChannel = event.getChannel().asTextChannel();
      TicketCloseResult result =
          components.ticketService().closeTicket(event.getMember(), settings, null, textChannel);
      event.reply(result.message()).setEphemeral(true).queue();
    }
  }

  private void handlePing(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().pingEnabled()) {
      event.reply("`/ping` is disabled.").setEphemeral(true).queue();
      return;
    }
    event
        .reply("Pong! Gateway ping: `" + event.getJDA().getGatewayPing() + "ms`")
        .setEphemeral(true)
        .queue();
  }

  private void handleLinks(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().linksEnabled()) {
      event.reply("`/links` is disabled.").setEphemeral(true).queue();
      return;
    }

    event
        .replyEmbeds(
            components
                .embedFactory()
                .base(components.config().branding().companyName() + " Resource Links")
                .setDescription("Documentation and support links for Arcane Arts products.")
                .addField(
                    "Adapt", "[Docs](" + components.config().branding().adaptDocsUrl() + ")", false)
                .addField(
                    "React", "[Docs](" + components.config().branding().reactDocsUrl() + ")", false)
                .addField(
                    "Iris", "[Docs](" + components.config().branding().irisDocsUrl() + ")", false)
                .addField(
                    "HoloUI",
                    "[Docs](" + components.config().branding().holouiDocsUrl() + ")",
                    false)
                .addField(
                    "Support",
                    "[Discord]("
                        + components.config().branding().supportInviteUrl()
                        + ")\n[Website]("
                        + components.config().branding().websiteUrl()
                        + ")\n[Patreon]("
                        + components.config().branding().patreonUrl()
                        + ")",
                    false)
                .build())
        .setEphemeral(true)
        .queue();
  }

  private void handleLogGuide(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().logGuideEnabled()) {
      event.reply("`/logguide` is disabled.").setEphemeral(true).queue();
      return;
    }

    event
        .replyEmbeds(
            components
                .embedFactory()
                .base("Log & Code Sharing Guide")
                .setDescription(
                    "Share complete logs and properly formatted code to speed up support.")
                .addField(
                    "Do",
                    "- Share full logs\n- Use paste services\n- Include version details",
                    false)
                .addField(
                    "Avoid",
                    "- Cropped screenshots\n- Partial snippets without context\n- Redacting stack traces",
                    false)
                .addField(
                    "Recommended",
                    "[mclo.gs](https://mclo.gs)\n[Pastebin](https://pastebin.com)\n[GitHub Gist](https://gist.github.com)",
                    false)
                .build())
        .setEphemeral(true)
        .queue();
  }

  private void handleSetup(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().setupEnabled()) {
      event.reply("`/setup` is disabled.").setEphemeral(true).queue();
      return;
    }
    if (event.getGuild() == null || event.getMember() == null) {
      event.reply("This command only works in a server.").setEphemeral(true).queue();
      return;
    }

    GuildSettings settings =
        components.setupService().getOrCreateSettings(event.getGuild().getIdLong());
    if (!components.permissionService().hasAdminAccess(event.getMember(), settings)) {
      event.reply("You need admin permission to run setup commands.").setEphemeral(true).queue();
      return;
    }

    String subcommand = event.getSubcommandName();
    if ("provision".equals(subcommand)) {
      SetupProvisionResult result = components.setupService().provision(event.getGuild());
      components.ticketService().ensureTicketHubPanel(event.getGuild(), result.settings());
      event
          .reply("Setup provision complete:\n- " + String.join("\n- ", result.actions()))
          .setEphemeral(true)
          .queue();
      return;
    }

    if ("audit".equals(subcommand)) {
      SetupAuditResult result = components.setupService().audit(event.getGuild());
      event
          .reply(
              (result.healthy() ? "Setup audit passed" : "Setup audit found issues")
                  + ":\n- "
                  + String.join("\n- ", result.findings()))
          .setEphemeral(true)
          .queue();
      return;
    }

    event.reply("Unknown `/setup` subcommand.").setEphemeral(true).queue();
  }

  private void handleNotify(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().notifyEnabled()) {
      event.reply("`/notify` is disabled.").setEphemeral(true).queue();
      return;
    }
    if (event.getGuild() == null || event.getMember() == null) {
      event.reply("This command only works in a server.").setEphemeral(true).queue();
      return;
    }

    UserPreferences preferences =
        components
            .notifyService()
            .getOrCreatePreferences(event.getGuild().getIdLong(), event.getMember().getIdLong());
    StringSelectMenu menu = components.notifyService().buildTopicMenu(preferences);

    event
        .reply("Select the update topics you want to subscribe to:")
        .addActionRow(menu)
        .setEphemeral(true)
        .queue();
  }

  private void handleTicket(SlashCommandInteractionEvent event) {
    BotRuntimeComponents components = runtime.get();
    if (!components.config().features().ticketEnabled()) {
      event.reply("`/ticket` is disabled.").setEphemeral(true).queue();
      return;
    }
    if (event.getGuild() == null || event.getMember() == null) {
      event.reply("This command only works in a server.").setEphemeral(true).queue();
      return;
    }

    Member member = event.getMember();
    GuildSettings settings =
        components.setupService().getOrCreateSettings(event.getGuild().getIdLong());

    String subcommand = event.getSubcommandName();
    if ("setup".equals(subcommand) || "panel".equals(subcommand)) {
      if (!components.permissionService().hasAdminAccess(member, settings)) {
        event
            .reply("You need admin permission to run `/ticket setup` or `/ticket panel`.")
            .setEphemeral(true)
            .queue();
        return;
      }
      SetupProvisionResult result = components.setupService().provision(event.getGuild());
      components.ticketService().ensureTicketHubPanel(event.getGuild(), result.settings());
      event
          .reply("Ticket setup complete:\n- " + String.join("\n- ", result.actions()))
          .setEphemeral(true)
          .queue();
      return;
    }

    if ("create".equals(subcommand)) {
      TicketCreateResult result = components.ticketService().createTicket(member, settings);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if ("claim".equals(subcommand)) {
      String ticketId =
          event.getOption("ticket_id") == null ? null : event.getOption("ticket_id").getAsString();
      TextChannel channel =
          event.getChannelType().isMessage() ? event.getChannel().asTextChannel() : null;
      TicketClaimResult result =
          components.ticketService().claimTicket(member, settings, ticketId, channel);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if ("close".equals(subcommand)) {
      String ticketId =
          event.getOption("ticket_id") == null ? null : event.getOption("ticket_id").getAsString();
      TextChannel channel =
          event.getChannelType().isMessage() ? event.getChannel().asTextChannel() : null;
      TicketCloseResult result =
          components.ticketService().closeTicket(member, settings, ticketId, channel);
      event.reply(result.message()).setEphemeral(true).queue();
      return;
    }

    if ("transcript".equals(subcommand)) {
      if (!components.permissionService().hasSupportAccess(member, settings)) {
        event
            .reply("You need support/admin permission to fetch transcripts.")
            .setEphemeral(true)
            .queue();
        return;
      }

      String ticketId = event.getOption("ticket_id").getAsString();
      var transcriptPath =
          components.ticketService().getTranscriptPath(event.getGuild().getIdLong(), ticketId);
      if (transcriptPath.isEmpty()) {
        event
            .reply("Transcript not found for ticket `" + ticketId + "`.")
            .setEphemeral(true)
            .queue();
        return;
      }
      replyWithTranscript(event, ticketId, transcriptPath.get());
      return;
    }

    if ("list".equals(subcommand)) {
      if (!components.permissionService().hasSupportAccess(member, settings)) {
        event
            .reply("You need support/admin permission to list open tickets.")
            .setEphemeral(true)
            .queue();
        return;
      }

      var openTickets = components.ticketService().listOpenTickets(event.getGuild().getIdLong());
      if (openTickets.isEmpty()) {
        event.reply("There are no open tickets right now.").setEphemeral(true).queue();
        return;
      }

      StringBuilder response = new StringBuilder("Open tickets:\n");
      openTickets.stream()
          .limit(25)
          .forEach(
              ticket -> {
                String channel =
                    ticket.getChannelId() == null
                        ? "`missing-channel`"
                        : "<#" + ticket.getChannelId() + ">";
                String claimedBy =
                    ticket.getClaimedByUserId() == null
                        ? "unclaimed"
                        : "claimed by <@" + ticket.getClaimedByUserId() + ">";
                response
                    .append("- `")
                    .append(ticket.getTicketId())
                    .append("` | ")
                    .append(channel)
                    .append(" | opener <@")
                    .append(ticket.getOpenerUserId())
                    .append("> | ")
                    .append(claimedBy)
                    .append("\n");
              });

      if (openTickets.size() > 25) {
        response.append("- ... and ").append(openTickets.size() - 25).append(" more");
      }

      event.reply(response.toString()).setEphemeral(true).queue();
      return;
    }

    event.reply("Unknown `/ticket` subcommand.").setEphemeral(true).queue();
  }

  private void replyWithTranscript(
      SlashCommandInteractionEvent event, String ticketId, Path transcriptPath) {
    event
        .reply("Transcript for ticket `" + ticketId + "`.")
        .addFiles(FileUpload.fromData(transcriptPath))
        .setEphemeral(true)
        .queue();
  }
}
