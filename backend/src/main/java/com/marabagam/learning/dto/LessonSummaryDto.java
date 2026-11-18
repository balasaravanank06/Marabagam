package com.marabagam.learning.dto;

import com.marabagam.learning.domain.Lesson;
import com.marabagam.learning.domain.LessonStatus;
import com.marabagam.learning.domain.LessonType;

import java.time.Instant;
import java.util.UUID;

public class LessonSummaryDto {
    private UUID id;
    private UUID moduleId;
    private String title;
    private String titleTa;
    private String description;
    private String descriptionTa;
    private LessonType lessonType;
    private Integer estimatedDurationMinutes;
    private Integer displayOrder;
    private LessonStatus status;
    private Instant createdAt;

    public LessonSummaryDto() {}

    public static LessonSummaryDto fromEntity(Lesson entity) {
        if (entity == null) return null;
        LessonSummaryDto dto = new LessonSummaryDto();
        dto.setId(entity.getId());
        if (entity.getModule() != null) dto.setModuleId(entity.getModule().getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleTa(entity.getTitleTa());
        dto.setDescription(entity.getDescription());
        dto.setDescriptionTa(entity.getDescriptionTa());
        dto.setLessonType(entity.getLessonType());
        dto.setEstimatedDurationMinutes(entity.getEstimatedDurationMinutes());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getModuleId() { return moduleId; }
    public void setModuleId(UUID moduleId) { this.moduleId = moduleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LessonStatus getStatus() { return status; }
    public void setStatus(LessonStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
