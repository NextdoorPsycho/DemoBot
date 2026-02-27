package art.arcane.demobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.config.CommandRegistrationMode;
import art.arcane.demobot.model.GuildSettings;
import art.arcane.demobot.service.PermissionService;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.Test;

class PermissionServiceTest {

  @Test
  void grantsAdminAccessWhenMemberHasConfiguredAdminRole() {
    BotConfig config = testConfig(Set.of());
    PermissionService permissionService = new PermissionService(config);

    Role adminRole = mock(Role.class);
    when(adminRole.getIdLong()).thenReturn(10L);

    Member member = mock(Member.class);
    when(member.getRoles()).thenReturn(List.of(adminRole));

    GuildSettings settings = GuildSettings.create(1L);
    settings.setAdminRoleIds(Set.of(10L));

    assertThat(permissionService.hasAdminAccess(member, settings)).isTrue();
  }

  @Test
  void fallsBackToManageServerPermissionWhenNoAdminRoleConfigured() {
    BotConfig config = testConfig(Set.of());
    PermissionService permissionService = new PermissionService(config);

    Member member = mock(Member.class);
    when(member.getRoles()).thenReturn(List.of());
    when(member.hasPermission(Permission.MANAGE_SERVER)).thenReturn(true);

    GuildSettings settings = GuildSettings.create(1L);
    settings.setAdminRoleIds(Set.of());

    assertThat(permissionService.hasAdminAccess(member, settings)).isTrue();
  }

  @Test
  void ownerBypassAppliesToSupportChecks() {
    BotConfig config = testConfig(Set.of(99L));
    PermissionService permissionService = new PermissionService(config);

    Member member = mock(Member.class);
    when(member.getIdLong()).thenReturn(99L);

    GuildSettings settings = GuildSettings.create(1L);
    settings.setSupportRoleIds(Set.of(50L));

    assertThat(permissionService.hasSupportAccess(member, settings)).isTrue();
  }

  private static BotConfig testConfig(Set<Long> ownerIds) {
    return new BotConfig(
        new BotConfig.Bot(
            "Arcane DemoBot",
            "watching:tests",
            net.dv8tion.jda.api.OnlineStatus.ONLINE,
            CommandRegistrationMode.GLOBAL,
            null),
        new BotConfig.Branding(
            "Arcane Arts",
            "#0EA5E9",
            "https://example.com/avatar.png",
            "https://example.com",
            "https://example.com/support",
            "https://example.com/patreon",
            "https://example.com/adapt",
            "https://example.com/react",
            "https://example.com/iris",
            "https://example.com/holoui"),
        new BotConfig.GuildDefaults(
            "Administrator", "Support", " - Notify", "Tickets", "ticket-hub", "ticket-logs"),
        new BotConfig.Tickets("ticket-", 10, 100, true, true),
        new BotConfig.Storage("./data", true),
        new BotConfig.Features(true, true, true, true, true, true),
        new BotConfig.Runtime("token", ownerIds, "INFO", Path.of("./data")));
  }
}
