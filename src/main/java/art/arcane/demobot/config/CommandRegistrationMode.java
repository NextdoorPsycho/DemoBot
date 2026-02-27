package art.arcane.demobot.config;

public enum CommandRegistrationMode {
  GLOBAL,
  GUILD;

  public static CommandRegistrationMode fromValue(String value) {
    if (value == null || value.isBlank()) {
      return GLOBAL;
    }
    try {
      return CommandRegistrationMode.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return GLOBAL;
    }
  }
}
