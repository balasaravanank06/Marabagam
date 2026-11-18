package com.marabagam.movement.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PracticeSessionResponseDto {

    private UUID id;
    private UUID userId;
    private UUID movementId;
    private String movementNameEn;
    private String movementNameTa;
    private String versionNumber;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private String status;
    private Double overallScore;
    private Integer durationSeconds;
    private List<PracticeFeedbackDto> feedbacks;
    private OffsetDateTime createdAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getMovementId() { return movementId; }
    public void setMovementId(UUID movementId) { this.movementId = movementId; }

    public String getMovementNameEn() { return movementNameEn; }
    public void setMovementNameEn(String movementNameEn) { this.movementNameEn = movementNameEn; }

    public String getMovementNameTa() { return movementNameTa; }
    public void setMovementNameTa(String movementNameTa) { this.movementNameTa = movementNameTa; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(OffsetDateTime startedAt) { this.startedAt = startedAt; }

    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public List<PracticeFeedbackDto> getFeedbacks() { return feedbacks; }
    public void setFeedbacks(List<PracticeFeedbackDto> feedbacks) { this.feedbacks = feedbacks; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
