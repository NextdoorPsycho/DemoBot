package art.arcane.demobot.service;

import art.arcane.demobot.model.TicketRecord;

public record TicketClaimResult(boolean success, String message, TicketRecord ticketRecord) {}
