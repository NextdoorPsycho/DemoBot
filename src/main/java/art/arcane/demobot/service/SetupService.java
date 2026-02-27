package art.arcane.demobot.service;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.model.GuildSettings;
import art.arcane.demobot.model.NotifyTopic;
import art.arcane.demobot.repository.GuildSettingsRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class SetupService {
  private final BotConfig config;
  private final GuildSettingsRepository guildSettingsRepository;

  public SetupService(BotConfig config, GuildSettingsRepository guildSettingsRepository) {
    this.config = config;
    this.guildSettingsRepository = guildSettingsRepository;
  }

  public GuildSettings getOrCreateSettings(long guildId) {
    return guildSettingsRepository
        .findByGuildId(guildId)
        .map(
            settings -> {
              settings.touch();
              return settings;
            })
        .orElseGet(() -> GuildSettings.create(guildId));
  }

  public SetupProvisionResult provision(Guild guild) {
    GuildSettings settings = getOrCreateSettings(guild.getIdLong());
    List<String> actions = new ArrayList<>();

    Role adminRole =
        ensureRole(
            guild, settings.getAdminRoleIds(), config.guildDefaults().adminRoleName(), actions);
    Role supportRole =
        ensureRole(
            guild, settings.getSupportRoleIds(), config.guildDefaults().supportRoleName(), actions);

    settings.setAdminRoleIds(new LinkedHashSet<>(Set.of(adminRole.getIdLong())));
    settings.setSupportRoleIds(new LinkedHashSet<>(Set.of(supportRole.getIdLong())));

    Map<String, Long> notifyRoles = new LinkedHashMap<>();
    for (NotifyTopic topic : NotifyTopic.values()) {
      Long existingRoleId = settings.getNotifyRoleIdsByTopic().get(topic.key());
      String roleName = topic.displayName() + config.guildDefaults().notifyRoleSuffix();
      Role role =
          ensureRole(
              guild, existingRoleId == null ? Set.of() : Set.of(existingRoleId), roleName, actions);
      notifyRoles.put(topic.key(), role.getIdLong());
    }
    settings.setNotifyRoleIdsByTopic(notifyRoles);

    Category ticketCategory =
        ensureCategory(
            guild,
            settings.getTicketCategoryId(),
            config.guildDefaults().ticketCategoryName(),
            actions);

    TextChannel ticketHub =
        ensureTextChannel(
            guild,
            settings.getTicketHubChannelId(),
            config.guildDefaults().ticketHubChannelName(),
            ticketCategory,
            actions);

    TextChannel ticketLog =
        ensureTextChannel(
            guild,
            settings.getTicketLogChannelId(),
            config.guildDefaults().ticketLogChannelName(),
            ticketCategory,
            actions);

    settings.setTicketCategoryId(ticketCategory.getIdLong());
    settings.setTicketHubChannelId(ticketHub.getIdLong());
    settings.setTicketLogChannelId(ticketLog.getIdLong());
    settings.touch();

    guildSettingsRepository.save(settings);
    if (actions.isEmpty()) {
      actions.add("No changes needed. Resources were already aligned.");
    }

    return new SetupProvisionResult(settings, actions);
  }

  public SetupAuditResult audit(Guild guild) {
    Optional<GuildSettings> existing = guildSettingsRepository.findByGuildId(guild.getIdLong());
    if (existing.isEmpty()) {
      return new SetupAuditResult(
          false, List.of("No guild settings found. Run /setup provision first."));
    }

    GuildSettings settings = existing.get();
    List<String> findings = new ArrayList<>();

    if (resolveRoleByIds(guild, settings.getAdminRoleIds()).isEmpty()) {
      findings.add("Missing admin role assignment from stored guild settings.");
    }
    if (resolveRoleByIds(guild, settings.getSupportRoleIds()).isEmpty()) {
      findings.add("Missing support role assignment from stored guild settings.");
    }

    for (NotifyTopic topic : NotifyTopic.values()) {
      Long roleId = settings.getNotifyRoleIdsByTopic().get(topic.key());
      if (roleId == null || guild.getRoleById(roleId) == null) {
        findings.add("Missing notify role for topic: " + topic.displayName());
      }
    }

    if (settings.getTicketCategoryId() == null
        || guild.getCategoryById(settings.getTicketCategoryId()) == null) {
      findings.add("Missing ticket category.");
    }

    if (settings.getTicketHubChannelId() == null
        || guild.getTextChannelById(settings.getTicketHubChannelId()) == null) {
      findings.add("Missing ticket hub channel.");
    }

    if (settings.getTicketLogChannelId() == null
        || guild.getTextChannelById(settings.getTicketLogChannelId()) == null) {
      findings.add("Missing ticket log channel.");
    }

    if (findings.isEmpty()) {
      findings.add("Audit passed. All managed resources are present.");
      return new SetupAuditResult(true, findings);
    }

    return new SetupAuditResult(false, findings);
  }

  private Role ensureRole(Guild guild, Set<Long> roleIds, String roleName, List<String> actions) {
    Optional<Role> roleById = resolveRoleByIds(guild, roleIds);
    Role role =
        roleById.orElseGet(
            () -> guild.getRolesByName(roleName, true).stream().findFirst().orElse(null));

    if (role == null) {
      Role created = guild.createRole().setName(roleName).complete();
      actions.add("Created role: " + roleName);
      return created;
    }

    if (!Objects.equals(role.getName(), roleName)) {
      role.getManager().setName(roleName).complete();
      actions.add("Renamed role to: " + roleName);
    }

    return role;
  }

  private Optional<Role> resolveRoleByIds(Guild guild, Set<Long> roleIds) {
    for (Long roleId : roleIds) {
      Role role = guild.getRoleById(roleId);
      if (role != null) {
        return Optional.of(role);
      }
    }
    return Optional.empty();
  }

  private Category ensureCategory(
      Guild guild, Long categoryId, String categoryName, List<String> actions) {
    Category category = categoryId == null ? null : guild.getCategoryById(categoryId);
    if (category == null) {
      category = guild.getCategoriesByName(categoryName, true).stream().findFirst().orElse(null);
    }

    if (category == null) {
      category = guild.createCategory(categoryName).complete();
      actions.add("Created category: " + categoryName);
    } else if (!Objects.equals(category.getName(), categoryName)) {
      category.getManager().setName(categoryName).complete();
      actions.add("Renamed category to: " + categoryName);
    }

    return category;
  }

  private TextChannel ensureTextChannel(
      Guild guild, Long channelId, String channelName, Category parent, List<String> actions) {
    TextChannel channel = channelId == null ? null : guild.getTextChannelById(channelId);
    if (channel == null) {
      channel = guild.getTextChannelsByName(channelName, true).stream().findFirst().orElse(null);
    }

    if (channel == null) {
      channel = guild.createTextChannel(channelName, parent).complete();
      actions.add("Created channel: #" + channelName);
      return channel;
    }

    if (!Objects.equals(channel.getName(), channelName)) {
      channel.getManager().setName(channelName).complete();
      actions.add("Renamed channel to: #" + channelName);
    }

    if (channel.getParentCategory() == null
        || channel.getParentCategory().getIdLong() != parent.getIdLong()) {
      channel.getManager().setParent(parent).complete();
      actions.add("Moved #" + channelName + " to category " + parent.getName());
    }

    return channel;
  }
}
