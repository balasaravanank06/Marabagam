package com.marabagam.learning.dto;

import com.marabagam.learning.domain.LessonProgress;

import java.time.Instant;
import java.util.UUID;

public class LessonProgressDto {
    private UUID id;
    private UUID userId;
    private UUID lessonId;
    private Integer progressPercent;
    private Boolean completed;
    private Instant startedAt;
    private Instant completedAt;
    private Instant lastAccessedAt;

    public LessonProgressDto() {}

    public static LessonProgressDto fromEntity(LessonProgress entity) {
        if (entity == null) return null;
        LessonProgressDto dto = new LessonProgressDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) dto.setUserId(entity.getUser().getId());
        if (entity.getLesson() != null) dto.setLessonId(entity.getLesson().getId());
        dto.setProgressPercent(entity.getProgressPercent());
        dto.setCompleted(entity.getCompleted());
        dto.setStartedAt(entity.getStartedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setLastAccessedAt(entity.getLastAccessedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(Instant lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
}
