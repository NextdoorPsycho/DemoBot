package art.arcane.demobot.config;

import java.nio.file.Path;
import java.util.Set;
import net.dv8tion.jda.api.OnlineStatus;

public record BotConfig(
    Bot bot,
    Branding branding,
    GuildDefaults guildDefaults,
    Tickets tickets,
    Storage storage,
    Features features,
    Runtime runtime) {

  public record Bot(
      String appName,
      String activity,
      OnlineStatus onlineStatus,
      CommandRegistrationMode commandRegistrationMode,
      Long devGuildId) {}

  public record Branding(
      String companyName,
      String primaryColorHex,
      String avatarUrl,
      String websiteUrl,
      String supportInviteUrl,
      String patreonUrl,
      String adaptDocsUrl,
      String reactDocsUrl,
      String irisDocsUrl,
      String holouiDocsUrl) {}

  public record GuildDefaults(
      String adminRoleName,
      String supportRoleName,
      String notifyRoleSuffix,
      String ticketCategoryName,
      String ticketHubChannelName,
      String ticketLogChannelName) {}

  public record Tickets(
      String channelPrefix,
      int creationCooldownSeconds,
      int transcriptMessageLimit,
      boolean deleteChannelOnClose,
      boolean postHubPanelOnProvision) {}

  public record Storage(String dataDir, boolean atomicWrites) {}

  public record Features(
      boolean pingEnabled,
      boolean linksEnabled,
      boolean logGuideEnabled,
      boolean notifyEnabled,
      boolean setupEnabled,
      boolean ticketEnabled) {}

  public record Runtime(
      String discordToken, Set<Long> ownerIds, String logLevel, Path dataDirPath) {}
}
