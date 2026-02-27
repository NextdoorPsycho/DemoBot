package art.arcane.demobot.repository;

import art.arcane.demobot.model.TicketRecord;
import java.util.List;
import java.util.Optional;

public interface TicketRepository {
  Optional<TicketRecord> findByGuildIdAndTicketId(long guildId, String ticketId);

  Optional<TicketRecord> findOpenByGuildIdAndUserId(long guildId, long userId);

  Optional<TicketRecord> findOpenByGuildIdAndChannelId(long guildId, long channelId);

  TicketRecord save(TicketRecord ticketRecord);

  List<TicketRecord> findByGuildId(long guildId);
}
