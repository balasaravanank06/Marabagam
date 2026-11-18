package com.marabagam.learning.dto;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.dto.ArtFormSummaryDto;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseDifficulty;
import com.marabagam.learning.domain.CourseStatus;

import java.time.Instant;
import java.util.UUID;

public class CourseSummaryDto {
    private UUID id;
    private ArtFormSummaryDto artForm;
    private String title;
    private String titleTa;
    private String slug;
    private String shortDescription;
    private String shortDescriptionTa;
    private CourseDifficulty difficulty;
    private Integer estimatedDurationMinutes;
    private CourseStatus status;
    private UUID createdById;
    private Instant createdAt;

    public CourseSummaryDto() {}

    public static CourseSummaryDto fromEntity(Course entity) {
        if (entity == null) return null;
        CourseSummaryDto dto = new CourseSummaryDto();
        dto.setId(entity.getId());
        if (entity.getArtForm() != null) {
            ArtForm af = entity.getArtForm();
            ArtFormSummaryDto afDto = new ArtFormSummaryDto();
            afDto.setId(af.getId());
            afDto.setName(af.getName());
            afDto.setNameTa(af.getNameTa());
            afDto.setSlug(af.getSlug());
            afDto.setShortDescription(af.getShortDescription());
            afDto.setShortDescriptionTa(af.getShortDescriptionTa());
            afDto.setOrigin(af.getOrigin());
            afDto.setOriginTa(af.getOriginTa());
            afDto.setStatus(af.getStatus());
            dto.setArtForm(afDto);
        }
        dto.setTitle(entity.getTitle());
        dto.setTitleTa(entity.getTitleTa());
        dto.setSlug(entity.getSlug());
        dto.setShortDescription(entity.getShortDescription());
        dto.setShortDescriptionTa(entity.getShortDescriptionTa());
        dto.setDifficulty(entity.getDifficulty());
        dto.setEstimatedDurationMinutes(entity.getEstimatedDurationMinutes());
        dto.setStatus(entity.getStatus());
        if (entity.getCreatedBy() != null) dto.setCreatedById(entity.getCreatedBy().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ArtFormSummaryDto getArtForm() { return artForm; }
    public void setArtForm(ArtFormSummaryDto artForm) { this.artForm = artForm; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }

    public String getShortDescriptionTa() { return shortDescriptionTa; }
    public void setShortDescriptionTa(String shortDescriptionTa) { this.shortDescriptionTa = shortDescriptionTa; }

    public CourseDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(CourseDifficulty difficulty) { this.difficulty = difficulty; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
