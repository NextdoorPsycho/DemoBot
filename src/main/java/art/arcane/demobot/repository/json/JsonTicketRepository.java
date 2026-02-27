package art.arcane.demobot.repository.json;

import art.arcane.demobot.model.TicketRecord;
import art.arcane.demobot.model.TicketStatus;
import art.arcane.demobot.repository.TicketRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JsonTicketRepository extends JsonRepositorySupport implements TicketRepository {

  public JsonTicketRepository(Path dataRoot, boolean atomicWrites) {
    super(dataRoot, atomicWrites);
  }

  @Override
  public Optional<TicketRecord> findByGuildIdAndTicketId(long guildId, String ticketId) {
    return read(pathFor(guildId, ticketId), TicketRecord.class);
  }

  @Override
  public Optional<TicketRecord> findOpenByGuildIdAndUserId(long guildId, long userId) {
    return findByGuildId(guildId).stream()
        .filter(record -> record.getStatus() == TicketStatus.OPEN)
        .filter(
            record ->
                record.getOpenerUserId() == userId
                    || record.getParticipantUserIds().contains(userId))
        .findFirst();
  }

  @Override
  public Optional<TicketRecord> findOpenByGuildIdAndChannelId(long guildId, long channelId) {
    return findByGuildId(guildId).stream()
        .filter(record -> record.getStatus() == TicketStatus.OPEN)
        .filter(record -> record.getChannelId() != null && record.getChannelId() == channelId)
        .findFirst();
  }

  @Override
  public TicketRecord save(TicketRecord ticketRecord) {
    return write(pathFor(ticketRecord.getGuildId(), ticketRecord.getTicketId()), ticketRecord);
  }

  @Override
  public List<TicketRecord> findByGuildId(long guildId) {
    Path guildTicketDir = dataRoot().resolve("tickets").resolve(Long.toString(guildId));
    List<TicketRecord> records = new ArrayList<>();
    for (Path path : listJsonFiles(guildTicketDir)) {
      read(path, TicketRecord.class).ifPresent(records::add);
    }
    records.sort(
        Comparator.comparing(
            TicketRecord::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
    return records;
  }

  private Path pathFor(long guildId, String ticketId) {
    return dataRoot()
        .resolve("tickets")
        .resolve(Long.toString(guildId))
        .resolve(ticketId + ".json");
  }
}
