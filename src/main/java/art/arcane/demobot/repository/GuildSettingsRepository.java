package art.arcane.demobot.repository;

import art.arcane.demobot.model.GuildSettings;
import java.util.Optional;

public interface GuildSettingsRepository {
  Optional<GuildSettings> findByGuildId(long guildId);

  GuildSettings save(GuildSettings settings);
}
