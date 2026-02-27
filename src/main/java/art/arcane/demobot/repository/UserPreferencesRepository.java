package art.arcane.demobot.repository;

import art.arcane.demobot.model.UserPreferences;
import java.util.Optional;

public interface UserPreferencesRepository {
  Optional<UserPreferences> findByGuildIdAndUserId(long guildId, long userId);

  UserPreferences save(UserPreferences preferences);
}
