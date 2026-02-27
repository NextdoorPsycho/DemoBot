package art.arcane.demobot.service;

import art.arcane.demobot.model.GuildSettings;
import art.arcane.demobot.model.NotifyTopic;
import art.arcane.demobot.model.UserPreferences;
import art.arcane.demobot.repository.UserPreferencesRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class NotifyService {
  public static final String NOTIFY_SELECT_ID = "notify:topic-select";

  private final UserPreferencesRepository userPreferencesRepository;

  public NotifyService(UserPreferencesRepository userPreferencesRepository) {
    this.userPreferencesRepository = userPreferencesRepository;
  }

  public UserPreferences getOrCreatePreferences(long guildId, long userId) {
    return userPreferencesRepository
        .findByGuildIdAndUserId(guildId, userId)
        .orElseGet(() -> UserPreferences.create(guildId, userId));
  }

  public StringSelectMenu buildTopicMenu(UserPreferences preferences) {
    Set<String> selected = new LinkedHashSet<>(preferences.getNotifyTopics());
    List<SelectOption> options = new ArrayList<>();
    for (NotifyTopic topic : NotifyTopic.values()) {
      SelectOption option =
          SelectOption.of(topic.displayName() + " Notifications", topic.key())
              .withDescription(topicDescription(topic));
      if (selected.contains(topic.key())) {
        option = option.withDefault(true);
      }
      options.add(option);
    }

    return StringSelectMenu.create(NOTIFY_SELECT_ID)
        .addOptions(options)
        .setMinValues(0)
        .setMaxValues(options.size())
        .build();
  }

  public List<String> applySelections(
      Member member, GuildSettings settings, List<String> selectedValues) {
    Set<String> selected = new LinkedHashSet<>();
    for (String value : selectedValues) {
      NotifyTopic.fromKey(value).ifPresent(topic -> selected.add(topic.key()));
    }

    List<String> changes = new ArrayList<>();
    for (NotifyTopic topic : NotifyTopic.values()) {
      Long roleId = settings.getNotifyRoleIdsByTopic().get(topic.key());
      if (roleId == null) {
        continue;
      }
      Role role = member.getGuild().getRoleById(roleId);
      if (role == null) {
        continue;
      }
      boolean wantsRole = selected.contains(topic.key());
      boolean hasRole = member.getRoles().stream().anyMatch(r -> r.getIdLong() == roleId);
      if (wantsRole && !hasRole) {
        member.getGuild().addRoleToMember(member, role).complete();
        changes.add("Enabled " + topic.displayName() + " notifications.");
      }
      if (!wantsRole && hasRole) {
        member.getGuild().removeRoleFromMember(member, role).complete();
        changes.add("Disabled " + topic.displayName() + " notifications.");
      }
    }

    UserPreferences preferences =
        getOrCreatePreferences(member.getGuild().getIdLong(), member.getIdLong());
    preferences.setNotifyTopics(selected);
    preferences.touch();
    userPreferencesRepository.save(preferences);

    if (changes.isEmpty()) {
      changes.add("No role changes were required.");
    }
    return changes;
  }

  private static String topicDescription(NotifyTopic topic) {
    return switch (topic) {
      case ADAPT -> "Adapt updates and announcements";
      case REACT -> "React updates and announcements";
      case IRIS -> "Iris updates and announcements";
      case HOLOUI -> "HoloUI updates and announcements";
      case NEWS -> "General Arcane Arts announcements";
    };
  }
}
