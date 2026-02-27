package art.arcane.demobot.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class TicketRecord {
  private String ticketId;
  private long guildId;
  private Long channelId;
  private long openerUserId;
  private Set<Long> participantUserIds = new LinkedHashSet<>();
  private TicketStatus status = TicketStatus.OPEN;
  private Instant createdAt;
  private Instant updatedAt;
  private Long claimedByUserId;
  private Instant claimedAt;
  private Instant closedAt;
  private Long closedByUserId;
  private String transcriptPath;

  public TicketRecord() {
    // Jackson constructor.
  }

  public static TicketRecord open(
      String ticketId, long guildId, long openerUserId, long channelId) {
    TicketRecord record = new TicketRecord();
    record.ticketId = ticketId;
    record.guildId = guildId;
    record.openerUserId = openerUserId;
    record.channelId = channelId;
    record.status = TicketStatus.OPEN;
    record.createdAt = Instant.now();
    record.updatedAt = record.createdAt;
    record.participantUserIds.add(openerUserId);
    return record;
  }

  public String getTicketId() {
    return ticketId;
  }

  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  public long getGuildId() {
    return guildId;
  }

  public void setGuildId(long guildId) {
    this.guildId = guildId;
  }

  public Long getChannelId() {
    return channelId;
  }

  public void setChannelId(Long channelId) {
    this.channelId = channelId;
  }

  public long getOpenerUserId() {
    return openerUserId;
  }

  public void setOpenerUserId(long openerUserId) {
    this.openerUserId = openerUserId;
  }

  public Set<Long> getParticipantUserIds() {
    return participantUserIds;
  }

  public void setParticipantUserIds(Set<Long> participantUserIds) {
    this.participantUserIds =
        participantUserIds == null ? new LinkedHashSet<>() : participantUserIds;
  }

  public TicketStatus getStatus() {
    return status;
  }

  public void setStatus(TicketStatus status) {
    this.status = status;
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

  public Long getClaimedByUserId() {
    return claimedByUserId;
  }

  public void setClaimedByUserId(Long claimedByUserId) {
    this.claimedByUserId = claimedByUserId;
  }

  public Instant getClaimedAt() {
    return claimedAt;
  }

  public void setClaimedAt(Instant claimedAt) {
    this.claimedAt = claimedAt;
  }

  public Instant getClosedAt() {
    return closedAt;
  }

  public void setClosedAt(Instant closedAt) {
    this.closedAt = closedAt;
  }

  public Long getClosedByUserId() {
    return closedByUserId;
  }

  public void setClosedByUserId(Long closedByUserId) {
    this.closedByUserId = closedByUserId;
  }

  public String getTranscriptPath() {
    return transcriptPath;
  }

  public void setTranscriptPath(String transcriptPath) {
    this.transcriptPath = transcriptPath;
  }
}
