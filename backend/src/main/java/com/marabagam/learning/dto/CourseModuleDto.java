package com.marabagam.learning.dto;

import com.marabagam.learning.domain.CourseModule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CourseModuleDto {
    private UUID id;
    private UUID courseId;
    private String title;
    private String titleTa;
    private String description;
    private String descriptionTa;
    private Integer displayOrder;
    private List<LessonSummaryDto> lessons = new ArrayList<>();
    private Instant createdAt;

    public CourseModuleDto() {}

    public static CourseModuleDto fromEntity(CourseModule entity, boolean publishedOnly) {
        if (entity == null) return null;
        CourseModuleDto dto = new CourseModuleDto();
        dto.setId(entity.getId());
        if (entity.getCourse() != null) dto.setCourseId(entity.getCourse().getId());
        dto.setTitle(entity.getTitle());
        dto.setTitleTa(entity.getTitleTa());
        dto.setDescription(entity.getDescription());
        dto.setDescriptionTa(entity.getDescriptionTa());
        dto.setDisplayOrder(entity.getDisplayOrder());
        if (entity.getLessons() != null) {
            dto.setLessons(entity.getLessons().stream()
                    .filter(l -> !publishedOnly || l.getStatus() == com.marabagam.learning.domain.LessonStatus.PUBLISHED)
                    .map(LessonSummaryDto::fromEntity)
                    .collect(Collectors.toList()));
        }
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public List<LessonSummaryDto> getLessons() { return lessons; }
    public void setLessons(List<LessonSummaryDto> lessons) { this.lessons = lessons; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
