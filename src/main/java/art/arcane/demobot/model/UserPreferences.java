package art.arcane.demobot.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserPreferences {
  private long guildId;
  private long userId;
  private Set<String> notifyTopics = new LinkedHashSet<>();
  private String openTicketId;
  private Instant createdAt;
  private Instant updatedAt;

  public UserPreferences() {
    // Jackson constructor.
  }

  public static UserPreferences create(long guildId, long userId) {
    UserPreferences preferences = new UserPreferences();
    preferences.guildId = guildId;
    preferences.userId = userId;
    Instant now = Instant.now();
    preferences.createdAt = now;
    preferences.updatedAt = now;
    return preferences;
  }

  public void touch() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    updatedAt = Instant.now();
  }

  public long getGuildId() {
    return guildId;
  }

  public void setGuildId(long guildId) {
    this.guildId = guildId;
  }

  public long getUserId() {
    return userId;
  }

  public void setUserId(long userId) {
    this.userId = userId;
  }

  public Set<String> getNotifyTopics() {
    return notifyTopics;
  }

  public void setNotifyTopics(Set<String> notifyTopics) {
    this.notifyTopics = notifyTopics == null ? new LinkedHashSet<>() : notifyTopics;
  }

  public String getOpenTicketId() {
    return openTicketId;
  }

  public void setOpenTicketId(String openTicketId) {
    this.openTicketId = openTicketId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
