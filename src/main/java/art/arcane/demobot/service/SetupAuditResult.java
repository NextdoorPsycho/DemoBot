package art.arcane.demobot.service;

import java.util.List;

public record SetupAuditResult(boolean healthy, List<String> findings) {}
