package art.arcane.demobot.model;

import java.util.Arrays;
import java.util.Optional;

public enum NotifyTopic {
  ADAPT("adapt", "Adapt"),
  REACT("react", "React"),
  IRIS("iris", "Iris"),
  HOLOUI("holoui", "HoloUI"),
  NEWS("news", "News");

  private final String key;
  private final String displayName;

  NotifyTopic(String key, String displayName) {
    this.key = key;
    this.displayName = displayName;
  }

  public String key() {
    return key;
  }

  public String displayName() {
    return displayName;
  }

  public static Optional<NotifyTopic> fromKey(String key) {
    return Arrays.stream(values()).filter(topic -> topic.key.equalsIgnoreCase(key)).findFirst();
  }
}
