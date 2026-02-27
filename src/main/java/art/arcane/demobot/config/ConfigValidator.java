package art.arcane.demobot.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class ConfigValidator {
  private static final Pattern HEX_COLOR = Pattern.compile("^#?[0-9a-fA-F]{6}$");

  private ConfigValidator() {}

  public static void validate(BotConfig config) {
    requireNonBlank(
        config.runtime().discordToken(),
        "runtime.discord_token is required. You can also pass -PdiscordToken=... for Gradle run tasks.");

    if (!HEX_COLOR.matcher(config.branding().primaryColorHex()).matches()) {
      throw new ConfigException(
          "branding.primary_color must be a 6 digit hex color (example: #0EA5E9)");
    }

    if (config.tickets().creationCooldownSeconds() < 0) {
      throw new ConfigException("tickets.creation_cooldown_seconds must be >= 0");
    }

    if (config.tickets().transcriptMessageLimit() <= 0) {
      throw new ConfigException("tickets.transcript_message_limit must be > 0");
    }

    validateUrl(config.branding().websiteUrl(), "branding.website_url");
    validateUrl(config.branding().supportInviteUrl(), "branding.support_invite_url");
    validateUrl(config.branding().patreonUrl(), "branding.patreon_url");
    validateUrl(config.branding().adaptDocsUrl(), "branding.adapt_docs_url");
    validateUrl(config.branding().reactDocsUrl(), "branding.react_docs_url");
    validateUrl(config.branding().irisDocsUrl(), "branding.iris_docs_url");
    validateUrl(config.branding().holouiDocsUrl(), "branding.holoui_docs_url");

    Path dataPath = config.runtime().dataDirPath();
    if (dataPath == null) {
      throw new ConfigException("storage.data_dir must resolve to a valid path.");
    }
  }

  private static void requireNonBlank(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new ConfigException(message);
    }
  }

  private static void validateUrl(String url, String key) {
    requireNonBlank(url, key + " is required.");
    try {
      URI parsed = new URI(url);
      if (parsed.getScheme() == null || parsed.getHost() == null) {
        throw new ConfigException(key + " must be a fully-qualified URL.");
      }
    } catch (URISyntaxException e) {
      throw new ConfigException(key + " must be a valid URL.", e);
    }
  }
}
