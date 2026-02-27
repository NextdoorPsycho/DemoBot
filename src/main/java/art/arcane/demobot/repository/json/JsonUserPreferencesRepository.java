package art.arcane.demobot.repository.json;

import art.arcane.demobot.model.UserPreferences;
import art.arcane.demobot.repository.UserPreferencesRepository;
import java.nio.file.Path;
import java.util.Optional;

public class JsonUserPreferencesRepository extends JsonRepositorySupport
    implements UserPreferencesRepository {

  public JsonUserPreferencesRepository(Path dataRoot, boolean atomicWrites) {
    super(dataRoot, atomicWrites);
  }

  @Override
  public Optional<UserPreferences> findByGuildIdAndUserId(long guildId, long userId) {
    return read(pathFor(guildId, userId), UserPreferences.class)
        .map(
            preferences -> {
              if (preferences.getGuildId() == 0L) {
                preferences.setGuildId(guildId);
              }
              if (preferences.getUserId() == 0L) {
                preferences.setUserId(userId);
              }
              return preferences;
            });
  }

  @Override
  public UserPreferences save(UserPreferences preferences) {
    preferences.touch();
    return write(pathFor(preferences.getGuildId(), preferences.getUserId()), preferences);
  }

  private Path pathFor(long guildId, long userId) {
    return dataRoot().resolve("users").resolve(Long.toString(guildId)).resolve(userId + ".json");
  }
}
