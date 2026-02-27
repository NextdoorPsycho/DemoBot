package art.arcane.demobot.app;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.service.EmbedFactory;
import art.arcane.demobot.service.NotifyService;
import art.arcane.demobot.service.PermissionService;
import art.arcane.demobot.service.SetupService;
import art.arcane.demobot.service.TicketService;

public record BotRuntimeComponents(
    BotConfig config,
    SetupService setupService,
    NotifyService notifyService,
    TicketService ticketService,
    PermissionService permissionService,
    EmbedFactory embedFactory) {}
