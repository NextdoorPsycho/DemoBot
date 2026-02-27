package art.arcane.demobot.service;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.model.GuildSettings;
import art.arcane.demobot.model.TicketRecord;
import art.arcane.demobot.model.TicketStatus;
import art.arcane.demobot.model.UserPreferences;
import art.arcane.demobot.repository.GuildSettingsRepository;
import art.arcane.demobot.repository.TicketRepository;
import art.arcane.demobot.repository.UserPreferencesRepository;
import art.arcane.demobot.util.TicketIdParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketService {
  public static final String CREATE_TICKET_BUTTON_ID = "ticket:create";
  public static final String OPEN_MY_TICKET_BUTTON_ID = "ticket:open-mine";
  public static final String CLAIM_TICKET_BUTTON_ID = "ticket:claim";
  public static final String CLOSE_TICKET_BUTTON_ID = "ticket:close";

  private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

  private final BotConfig config;
  private final TicketRepository ticketRepository;
  private final UserPreferencesRepository userPreferencesRepository;
  private final GuildSettingsRepository guildSettingsRepository;
  private final PermissionService permissionService;
  private final EmbedFactory embedFactory;
  private final Path dataRoot;
  private final Map<Long, Instant> ticketCreateCooldownByUserId = new ConcurrentHashMap<>();

  public TicketService(
      BotConfig config,
      TicketRepository ticketRepository,
      UserPreferencesRepository userPreferencesRepository,
      GuildSettingsRepository guildSettingsRepository,
      PermissionService permissionService,
      EmbedFactory embedFactory) {
    this.config = config;
    this.ticketRepository = ticketRepository;
    this.userPreferencesRepository = userPreferencesRepository;
    this.guildSettingsRepository = guildSettingsRepository;
    this.permissionService = permissionService;
    this.embedFactory = embedFactory;
    this.dataRoot = config.runtime().dataDirPath();
  }

  public void ensureTicketHubPanel(Guild guild, GuildSettings settings) {
    if (!config.tickets().postHubPanelOnProvision() || settings.getTicketHubChannelId() == null) {
      return;
    }

    TextChannel hubChannel = guild.getTextChannelById(settings.getTicketHubChannelId());
    if (hubChannel == null) {
      return;
    }

    long openCount = listOpenTickets(guild.getIdLong()).size();
    Message panel = resolveExistingPanelMessage(hubChannel, settings);

    if (panel != null) {
      panel
          .editMessageEmbeds(buildHubPanelEmbed(openCount).build())
          .setActionRow(
              Button.primary(CREATE_TICKET_BUTTON_ID, "Create Ticket"),
              Button.secondary(OPEN_MY_TICKET_BUTTON_ID, "Open My Ticket"))
          .complete();
      if (settings.getTicketPanelMessageId() == null
          || settings.getTicketPanelMessageId() != panel.getIdLong()) {
        settings.setTicketPanelMessageId(panel.getIdLong());
        settings.touch();
        guildSettingsRepository.save(settings);
      }
      return;
    }

    Message created =
        hubChannel
            .sendMessageEmbeds(buildHubPanelEmbed(openCount).build())
            .setActionRow(
                Button.primary(CREATE_TICKET_BUTTON_ID, "Create Ticket"),
                Button.secondary(OPEN_MY_TICKET_BUTTON_ID, "Open My Ticket"))
            .complete();
    created.pin().queue(success -> {}, failure -> {});

    settings.setTicketPanelMessageId(created.getIdLong());
    settings.touch();
    guildSettingsRepository.save(settings);
  }

  public TicketCreateResult openOrCreateTicket(Member member, GuildSettings settings) {
    Optional<TicketRecord> existing = findOpenTicketForMember(member);
    if (existing.isPresent()) {
      return existingTicketResult(member.getGuild(), existing.get());
    }
    return createTicket(member, settings);
  }

  public TicketCreateResult createTicket(Member member, GuildSettings settings) {
    if (isInCooldown(member.getIdLong())) {
      return new TicketCreateResult(false, "Please wait before creating another ticket.", null);
    }

    Optional<TicketRecord> existing = findOpenTicketForMember(member);
    if (existing.isPresent()) {
      return existingTicketResult(member.getGuild(), existing.get());
    }

    Category ticketCategory = resolveTicketCategory(member.getGuild(), settings);
    if (ticketCategory == null) {
      return new TicketCreateResult(
          false,
          "Ticket category is missing. Run `/ticket setup` or `/setup provision` first.",
          null);
    }

    String ticketId = nextTicketId(member.getGuild().getIdLong(), settings);
    String ticketChannelName = config.tickets().channelPrefix() + ticketId;

    var action =
        member
            .getGuild()
            .createTextChannel(ticketChannelName, ticketCategory)
            .addRolePermissionOverride(
                member.getGuild().getPublicRole().getIdLong(),
                null,
                EnumSet.of(Permission.VIEW_CHANNEL));

    for (Long roleId : settings.getAdminRoleIds()) {
      action =
          action.addRolePermissionOverride(
              roleId,
              EnumSet.of(
                  Permission.VIEW_CHANNEL,
                  Permission.MESSAGE_SEND,
                  Permission.MESSAGE_HISTORY,
                  Permission.MESSAGE_ATTACH_FILES),
              null);
    }

    for (Long roleId : settings.getSupportRoleIds()) {
      action =
          action.addRolePermissionOverride(
              roleId,
              EnumSet.of(
                  Permission.VIEW_CHANNEL,
                  Permission.MESSAGE_SEND,
                  Permission.MESSAGE_HISTORY,
                  Permission.MESSAGE_ATTACH_FILES),
              null);
    }

    TextChannel ticketChannel =
        action
            .addMemberPermissionOverride(
                member.getIdLong(),
                EnumSet.of(
                    Permission.VIEW_CHANNEL,
                    Permission.MESSAGE_SEND,
                    Permission.MESSAGE_HISTORY,
                    Permission.MESSAGE_ATTACH_FILES),
                null)
            .complete();

    TicketRecord record =
        TicketRecord.open(
            ticketId, member.getGuild().getIdLong(), member.getIdLong(), ticketChannel.getIdLong());
    record.setUpdatedAt(Instant.now());
    ticketRepository.save(record);

    UserPreferences preferences =
        getOrCreatePreferences(member.getGuild().getIdLong(), member.getIdLong());
    preferences.setOpenTicketId(ticketId);
    preferences.touch();
    userPreferencesRepository.save(preferences);

    ticketChannel.getManager().setTopic(buildChannelTopic(record)).queue();
    ticketChannel.sendMessage(member.getAsMention() + " your ticket is ready.").queue();
    ticketChannel
        .sendMessageEmbeds(
            embedFactory
                .base("Ticket " + ticketId)
                .setDescription(
                    "Describe your issue with as much detail as possible. Support staff can claim ownership using the button below.")
                .addField("Owner", member.getAsMention(), true)
                .addField("Status", "Open / Unclaimed", true)
                .build())
        .setActionRow(
            Button.secondary(CLAIM_TICKET_BUTTON_ID, "Claim Ticket"),
            Button.danger(CLOSE_TICKET_BUTTON_ID, "Close Ticket"))
        .queue();

    ticketCreateCooldownByUserId.put(member.getIdLong(), Instant.now());
    ensureTicketHubPanel(member.getGuild(), settings);
    return new TicketCreateResult(true, "Ticket created: " + ticketChannel.getAsMention(), record);
  }

  public TicketClaimResult claimTicket(
      Member actor, GuildSettings settings, String ticketId, TextChannel currentChannel) {
    String resolvedId = resolveTicketId(ticketId, currentChannel);
    if (resolvedId == null) {
      return new TicketClaimResult(
          false,
          "Could not determine ticket ID. Provide `ticket_id` or run this in a ticket channel.",
          null);
    }

    Optional<TicketRecord> ticketRecordOptional =
        ticketRepository.findByGuildIdAndTicketId(actor.getGuild().getIdLong(), resolvedId);
    if (ticketRecordOptional.isEmpty()) {
      return new TicketClaimResult(false, "Ticket not found: " + resolvedId, null);
    }

    TicketRecord ticketRecord = ticketRecordOptional.get();
    if (ticketRecord.getStatus() != TicketStatus.OPEN) {
      return new TicketClaimResult(false, "This ticket is already closed.", ticketRecord);
    }

    boolean hasSupport =
        permissionService.hasSupportAccess(actor, settings) || permissionService.isOwner(actor);
    if (!hasSupport) {
      return new TicketClaimResult(
          false, "You need support/admin permission to claim tickets.", ticketRecord);
    }

    Long currentAssignee = ticketRecord.getClaimedByUserId();
    boolean isAdmin =
        permissionService.hasAdminAccess(actor, settings) || permissionService.isOwner(actor);
    if (currentAssignee != null && currentAssignee != actor.getIdLong() && !isAdmin) {
      return new TicketClaimResult(
          false, "Ticket is already claimed by <@" + currentAssignee + ">.", ticketRecord);
    }

    ticketRecord.setClaimedByUserId(actor.getIdLong());
    ticketRecord.setClaimedAt(Instant.now());
    ticketRecord.setUpdatedAt(Instant.now());
    ticketRecord.getParticipantUserIds().add(actor.getIdLong());
    ticketRepository.save(ticketRecord);

    TextChannel ticketChannel = actor.getGuild().getTextChannelById(ticketRecord.getChannelId());
    if (ticketChannel != null) {
      ticketChannel.getManager().setTopic(buildChannelTopic(ticketRecord)).queue();
      ticketChannel
          .sendMessage(
              "Ticket `" + ticketRecord.getTicketId() + "` claimed by " + actor.getAsMention())
          .queue();
    }

    return new TicketClaimResult(
        true, "Ticket claimed: `" + ticketRecord.getTicketId() + "`", ticketRecord);
  }

  public TicketCloseResult closeTicket(
      Member actor, GuildSettings settings, String ticketId, TextChannel currentChannel) {
    String resolvedId = resolveTicketId(ticketId, currentChannel);
    if (resolvedId == null) {
      return new TicketCloseResult(
          false,
          "Could not determine ticket ID. Provide `ticket_id` or run this in a ticket channel.",
          null,
          null);
    }

    Optional<TicketRecord> ticketRecordOptional =
        ticketRepository.findByGuildIdAndTicketId(actor.getGuild().getIdLong(), resolvedId);
    if (ticketRecordOptional.isEmpty()) {
      return new TicketCloseResult(false, "Ticket not found: " + resolvedId, null, null);
    }

    TicketRecord ticketRecord = ticketRecordOptional.get();
    if (ticketRecord.getStatus() == TicketStatus.CLOSED) {
      return new TicketCloseResult(
          false,
          "This ticket is already closed.",
          ticketRecord,
          resolveTranscriptPath(ticketRecord).orElse(null));
    }

    boolean isOpener = actor.getIdLong() == ticketRecord.getOpenerUserId();
    boolean hasSupport =
        permissionService.hasSupportAccess(actor, settings) || permissionService.isOwner(actor);
    if (!isOpener && !hasSupport) {
      return new TicketCloseResult(
          false, "You do not have permission to close this ticket.", ticketRecord, null);
    }

    Guild guild = actor.getGuild();
    TextChannel ticketChannel = guild.getTextChannelById(ticketRecord.getChannelId());
    if (ticketChannel == null && currentChannel != null) {
      ticketChannel = currentChannel;
    }

    Path transcriptPath = null;
    if (ticketChannel != null) {
      transcriptPath = createTranscript(ticketChannel, ticketRecord);
    }

    ticketRecord.setStatus(TicketStatus.CLOSED);
    ticketRecord.setClosedAt(Instant.now());
    ticketRecord.setClosedByUserId(actor.getIdLong());
    ticketRecord.setUpdatedAt(Instant.now());
    ticketRecord.setTranscriptPath(transcriptPath == null ? null : transcriptPath.toString());
    ticketRepository.save(ticketRecord);

    clearOpenTicketFlag(
        ticketRecord.getGuildId(), ticketRecord.getOpenerUserId(), ticketRecord.getTicketId());

    if (transcriptPath != null) {
      postTranscriptToLogChannel(guild, settings, ticketRecord, transcriptPath);
    }

    if (ticketChannel != null) {
      if (config.tickets().deleteChannelOnClose()) {
        ticketChannel.delete().reason("Ticket closed: " + resolvedId).queue();
      } else {
        ticketChannel
            .getManager()
            .setName(buildClosedChannelName(ticketRecord.getTicketId()))
            .setTopic(buildChannelTopic(ticketRecord))
            .queue();
        ticketChannel
            .sendMessage("Ticket closed by <@" + actor.getIdLong() + ">. This channel is archived.")
            .queue();
      }
    }

    ensureTicketHubPanel(guild, settings);
    return new TicketCloseResult(
        true, "Ticket closed: " + resolvedId, ticketRecord, transcriptPath);
  }

  public List<TicketRecord> listOpenTickets(long guildId) {
    return ticketRepository.findByGuildId(guildId).stream()
        .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN)
        .sorted(
            Comparator.comparing(
                TicketRecord::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
        .toList();
  }

  public Optional<Path> getTranscriptPath(long guildId, String ticketId) {
    return ticketRepository
        .findByGuildIdAndTicketId(guildId, ticketId)
        .flatMap(record -> Optional.ofNullable(record.getTranscriptPath()))
        .map(Path::of)
        .filter(Files::exists);
  }

  private String resolveTicketId(String ticketId, TextChannel currentChannel) {
    if (ticketId != null && !ticketId.isBlank()) {
      return ticketId;
    }
    if (currentChannel == null) {
      return null;
    }
    return TicketIdParser.fromChannelName(
            currentChannel.getName(), config.tickets().channelPrefix())
        .orElse(null);
  }

  private Optional<TicketRecord> findOpenTicketForMember(Member member) {
    long guildId = member.getGuild().getIdLong();
    long userId = member.getIdLong();

    UserPreferences preferences = getOrCreatePreferences(guildId, userId);
    String openTicketId = preferences.getOpenTicketId();

    if (openTicketId != null && !openTicketId.isBlank()) {
      Optional<TicketRecord> fromPreference =
          ticketRepository
              .findByGuildIdAndTicketId(guildId, openTicketId)
              .filter(ticket -> ticket.getStatus() == TicketStatus.OPEN);
      if (fromPreference.isPresent()) {
        return fromPreference;
      }

      preferences.setOpenTicketId(null);
      preferences.touch();
      userPreferencesRepository.save(preferences);
    }

    Optional<TicketRecord> fallback = ticketRepository.findOpenByGuildIdAndUserId(guildId, userId);
    fallback.ifPresent(
        ticket -> {
          preferences.setOpenTicketId(ticket.getTicketId());
          preferences.touch();
          userPreferencesRepository.save(preferences);
        });
    return fallback;
  }

  private TicketCreateResult existingTicketResult(Guild guild, TicketRecord record) {
    TextChannel channel =
        record.getChannelId() == null ? null : guild.getTextChannelById(record.getChannelId());
    if (channel != null) {
      return new TicketCreateResult(
          false,
          "You already have an open ticket: "
              + channel.getAsMention()
              + " (`"
              + record.getTicketId()
              + "`).",
          record);
    }
    return new TicketCreateResult(
        false,
        "You already have an open ticket (`"
            + record.getTicketId()
            + "`), but the channel is missing. Ask staff to close it.",
        record);
  }

  private UserPreferences getOrCreatePreferences(long guildId, long userId) {
    return userPreferencesRepository
        .findByGuildIdAndUserId(guildId, userId)
        .orElseGet(() -> UserPreferences.create(guildId, userId));
  }

  private void clearOpenTicketFlag(long guildId, long userId, String ticketId) {
    userPreferencesRepository
        .findByGuildIdAndUserId(guildId, userId)
        .ifPresent(
            preferences -> {
              if (ticketId.equals(preferences.getOpenTicketId())) {
                preferences.setOpenTicketId(null);
                preferences.touch();
                userPreferencesRepository.save(preferences);
              }
            });
  }

  private Path createTranscript(TextChannel ticketChannel, TicketRecord ticketRecord) {
    if (config.tickets().transcriptMessageLimit() <= 0) {
      return null;
    }

    try {
      List<Message> messages =
          new ArrayList<>(
              ticketChannel
                  .getIterableHistory()
                  .takeAsync(config.tickets().transcriptMessageLimit())
                  .join());
      messages.sort(Comparator.comparing(Message::getTimeCreated));

      StringBuilder transcript = new StringBuilder();
      transcript
          .append("# Ticket Transcript\n\n")
          .append("- Ticket ID: ")
          .append(ticketRecord.getTicketId())
          .append('\n')
          .append("- Guild ID: ")
          .append(ticketRecord.getGuildId())
          .append('\n')
          .append("- Opener ID: ")
          .append(ticketRecord.getOpenerUserId())
          .append('\n')
          .append("- Created At: ")
          .append(ticketRecord.getCreatedAt())
          .append('\n')
          .append("- Claimed By: ")
          .append(
              ticketRecord.getClaimedByUserId() == null
                  ? "Unclaimed"
                  : ticketRecord.getClaimedByUserId())
          .append("\n\n")
          .append("---\n\n");

      for (Message message : messages) {
        OffsetDateTime time = message.getTimeCreated();
        transcript
            .append("[")
            .append(time)
            .append("] ")
            .append(message.getAuthor().getName())
            .append(" (")
            .append(message.getAuthor().getId())
            .append("): ")
            .append(message.getContentDisplay())
            .append("\n");

        if (!message.getAttachments().isEmpty()) {
          transcript.append("  Attachments:\n");
          message
              .getAttachments()
              .forEach(
                  attachment -> transcript.append("  - ").append(attachment.getUrl()).append('\n'));
        }
      }

      Path transcriptPath =
          dataRoot
              .resolve("transcripts")
              .resolve(Long.toString(ticketRecord.getGuildId()))
              .resolve(ticketRecord.getTicketId() + ".md");
      Files.createDirectories(transcriptPath.getParent());
      Files.writeString(transcriptPath, transcript.toString(), StandardCharsets.UTF_8);
      return transcriptPath;
    } catch (Exception exception) {
      LOGGER.error(
          "Failed to generate transcript for ticket {}", ticketRecord.getTicketId(), exception);
      return null;
    }
  }

  private void postTranscriptToLogChannel(
      Guild guild, GuildSettings settings, TicketRecord ticketRecord, Path transcriptPath) {
    if (settings.getTicketLogChannelId() == null || !Files.exists(transcriptPath)) {
      return;
    }

    TextChannel logChannel = guild.getTextChannelById(settings.getTicketLogChannelId());
    if (logChannel == null) {
      return;
    }

    logChannel
        .sendMessage("Transcript for ticket `" + ticketRecord.getTicketId() + "`")
        .addFiles(FileUpload.fromData(transcriptPath))
        .queue();
  }

  private Optional<Path> resolveTranscriptPath(TicketRecord ticketRecord) {
    if (ticketRecord.getTranscriptPath() == null) {
      return Optional.empty();
    }
    Path transcriptPath = Path.of(ticketRecord.getTranscriptPath());
    if (Files.exists(transcriptPath)) {
      return Optional.of(transcriptPath);
    }
    return Optional.empty();
  }

  private boolean isInCooldown(long userId) {
    Instant previous = ticketCreateCooldownByUserId.get(userId);
    if (previous == null) {
      return false;
    }
    Duration elapsed = Duration.between(previous, Instant.now());
    return elapsed.getSeconds() < config.tickets().creationCooldownSeconds();
  }

  private Category resolveTicketCategory(Guild guild, GuildSettings settings) {
    if (settings.getTicketCategoryId() == null) {
      return guild.getCategoriesByName(config.guildDefaults().ticketCategoryName(), true).stream()
          .findFirst()
          .orElse(null);
    }

    Category byId = guild.getCategoryById(settings.getTicketCategoryId());
    if (byId != null) {
      return byId;
    }

    return guild.getCategoriesByName(config.guildDefaults().ticketCategoryName(), true).stream()
        .findFirst()
        .orElse(null);
  }

  private synchronized String nextTicketId(long guildId, GuildSettings settings) {
    int sequence =
        settings.getNextTicketSequence() == null
            ? 1
            : Math.max(1, settings.getNextTicketSequence());
    String candidate = String.format("%04d", sequence);

    while (ticketRepository.findByGuildIdAndTicketId(guildId, candidate).isPresent()) {
      sequence++;
      candidate = String.format("%04d", sequence);
    }

    settings.setNextTicketSequence(sequence + 1);
    settings.touch();
    guildSettingsRepository.save(settings);
    return candidate;
  }

  private Message resolveExistingPanelMessage(TextChannel hubChannel, GuildSettings settings) {
    if (settings.getTicketPanelMessageId() != null) {
      try {
        return hubChannel.retrieveMessageById(settings.getTicketPanelMessageId()).complete();
      } catch (Exception ignored) {
        // Fallback search below.
      }
    }

    return hubChannel.getHistory().retrievePast(50).complete().stream()
        .filter(message -> message.getAuthor().equals(hubChannel.getJDA().getSelfUser()))
        .filter(
            message ->
                message.getButtons().stream()
                    .anyMatch(button -> CREATE_TICKET_BUTTON_ID.equals(button.getId())))
        .findFirst()
        .orElse(null);
  }

  private net.dv8tion.jda.api.EmbedBuilder buildHubPanelEmbed(long openCount) {
    return embedFactory
        .base("Support Ticket Center")
        .setDescription(
            "Use **Create Ticket** to open a new support ticket.\n"
                + "Use **Open My Ticket** to jump to your existing one if already open.")
        .addField("Current Open Tickets", Long.toString(openCount), true)
        .addField("Ticket Policy", "One open ticket per user.", true);
  }

  private String buildChannelTopic(TicketRecord record) {
    StringBuilder topic =
        new StringBuilder("ticket=")
            .append(record.getTicketId())
            .append(" | opener=")
            .append(record.getOpenerUserId());

    if (record.getClaimedByUserId() != null) {
      topic.append(" | claimed=").append(record.getClaimedByUserId());
    } else {
      topic.append(" | claimed=none");
    }

    topic.append(" | status=").append(record.getStatus().name().toLowerCase());

    String built = topic.toString();
    return built.length() <= 1024 ? built : built.substring(0, 1024);
  }

  private String buildClosedChannelName(String ticketId) {
    String name = "closed-" + config.tickets().channelPrefix() + ticketId;
    return name.length() <= 100 ? name : name.substring(0, 100);
  }
}
