package art.arcane.demobot;

import static org.assertj.core.api.Assertions.assertThat;

import art.arcane.demobot.model.UserPreferences;
import art.arcane.demobot.repository.json.JsonUserPreferencesRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class JsonUserPreferencesRepositoryTest {

  @Test
  void savesAndLoadsPreferences() throws Exception {
    Path tempDir = Files.createTempDirectory("demobot-userprefs");
    JsonUserPreferencesRepository repository = new JsonUserPreferencesRepository(tempDir, true);

    UserPreferences preferences = UserPreferences.create(1L, 2L);
    preferences.getNotifyTopics().add("adapt");

    repository.save(preferences);

    var loaded = repository.findByGuildIdAndUserId(1L, 2L);
    assertThat(loaded).isPresent();
    assertThat(loaded.orElseThrow().getNotifyTopics()).contains("adapt");
  }

  @Test
  void quarantinesCorruptJson() throws Exception {
    Path tempDir = Files.createTempDirectory("demobot-corrupt");
    JsonUserPreferencesRepository repository = new JsonUserPreferencesRepository(tempDir, true);

    Path target = tempDir.resolve("users").resolve("123").resolve("456.json");
    Files.createDirectories(target.getParent());
    Files.writeString(target, "{not-json}");

    var loaded = repository.findByGuildIdAndUserId(123L, 456L);
    assertThat(loaded).isEmpty();

    Path quarantineDir = tempDir.resolve("quarantine");
    assertThat(Files.exists(quarantineDir)).isTrue();
    try (var stream = Files.list(quarantineDir)) {
      assertThat(stream.map(Path::getFileName).map(Path::toString).toList()).isNotEmpty();
    }

    Files.walk(tempDir)
        .sorted(Comparator.reverseOrder())
        .forEach(
            path -> {
              try {
                Files.deleteIfExists(path);
              } catch (Exception ignored) {
              }
            });
  }
}
