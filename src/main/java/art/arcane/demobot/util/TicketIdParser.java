package art.arcane.demobot.util;

import java.util.Optional;

public final class TicketIdParser {
  private TicketIdParser() {}

  public static Optional<String> fromChannelName(String channelName, String ticketPrefix) {
    if (channelName == null || channelName.isBlank()) {
      return Optional.empty();
    }
    if (!channelName.startsWith(ticketPrefix)) {
      return Optional.empty();
    }
    String ticketId = channelName.substring(ticketPrefix.length()).trim();
    if (ticketId.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(ticketId);
  }
}
