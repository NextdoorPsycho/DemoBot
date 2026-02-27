package art.arcane.demobot.service;

import art.arcane.demobot.model.GuildSettings;
import java.util.List;

public record SetupProvisionResult(GuildSettings settings, List<String> actions) {}
