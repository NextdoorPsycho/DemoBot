package art.arcane.demobot.service;

import art.arcane.demobot.model.TicketRecord;

public record TicketCreateResult(boolean success, String message, TicketRecord ticketRecord) {}
