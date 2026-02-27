package art.arcane.demobot.service;

import art.arcane.demobot.model.TicketRecord;
import java.nio.file.Path;

public record TicketCloseResult(
    boolean success, String message, TicketRecord ticketRecord, Path transcriptPath) {}
