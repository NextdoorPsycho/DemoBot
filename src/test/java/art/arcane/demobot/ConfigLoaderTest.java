package art.arcane.demobot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import art.arcane.demobot.config.BotConfig;
import art.arcane.demobot.config.ConfigException;
import art.arcane.demobot.config.ConfigLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ConfigLoaderTest {

  @Test
  void loadsTomlRuntimeValues() throws Exception {
    Path tempConfig = Files.createTempFile("demobot-config", ".toml");
    Files.writeString(
        tempConfig,
        """
                [bot]
                app_name = "Test Bot"

                [branding]
                company_name = "Arcane Arts"
                primary_color = "#11AAFF"
                avatar_url = "https://example.com/avatar.png"
                website_url = "https://example.com"
                support_invite_url = "https://example.com/support"
                patreon_url = "https://example.com/patreon"
                adapt_docs_url = "https://example.com/adapt"
                react_docs_url = "https://example.com/react"
                iris_docs_url = "https://example.com/iris"
                holoui_docs_url = "https://example.com/holoui"

                [storage]
                data_dir = "./test-data"

                [runtime]
                discord_token = "abc123"
                owner_ids = [1, "2"]
                log_level = "INFO"
                """);

    ConfigLoader loader = new ConfigLoader();
    BotConfig config = loader.load(tempConfig);

    assertThat(config.bot().appName()).isEqualTo("Test Bot");
    assertThat(config.runtime().discordToken()).isEqualTo("abc123");
    assertThat(config.runtime().ownerIds()).containsExactlyInAnyOrder(1L, 2L);
    assertThat(config.runtime().dataDirPath().toString()).contains("test-data");
  }

  @Test
  void rejectsInvalidColor() throws Exception {
    Path tempConfig = Files.createTempFile("demobot-invalid-config", ".toml");
    Files.writeString(
        tempConfig,
        """
                [branding]
                primary_color = "not-a-color"
                company_name = "Arcane Arts"
                avatar_url = "https://example.com/avatar.png"
                website_url = "https://example.com"
                support_invite_url = "https://example.com/support"
                patreon_url = "https://example.com/patreon"
                adapt_docs_url = "https://example.com/adapt"
                react_docs_url = "https://example.com/react"
                iris_docs_url = "https://example.com/iris"
                holoui_docs_url = "https://example.com/holoui"

                [runtime]
                discord_token = "abc123"
                """);

    ConfigLoader loader = new ConfigLoader();

    assertThatThrownBy(() -> loader.load(tempConfig))
        .isInstanceOf(ConfigException.class)
        .hasMessageContaining("primary_color");
  }

  @Test
  void generatesTemplateConfigFilesWhenMissing() throws Exception {
    Path tempDir = Files.createTempDirectory("demobot-bootstrap");
    Path configFile = tempDir.resolve("config").resolve("bot.toml");

    ConfigLoader loader = new ConfigLoader();

    assertThatThrownBy(() -> loader.load(configFile))
        .isInstanceOf(ConfigException.class)
        .hasMessageContaining("runtime.discord_token is required");

    assertThat(Files.exists(configFile)).isTrue();
    assertThat(Files.exists(configFile.resolveSibling("bot.toml.example"))).isTrue();
    assertThat(Files.readString(configFile)).contains("[runtime]");
  }
}
