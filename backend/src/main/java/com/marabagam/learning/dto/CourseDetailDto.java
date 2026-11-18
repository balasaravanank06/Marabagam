package com.marabagam.learning.dto;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.dto.ArtFormSummaryDto;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseDifficulty;
import com.marabagam.learning.domain.CourseStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CourseDetailDto {
    private UUID id;
    private ArtFormSummaryDto artForm;
    private String title;
    private String titleTa;
    private String slug;
    private String shortDescription;
    private String shortDescriptionTa;
    private String description;
    private String descriptionTa;
    private CourseDifficulty difficulty;
    private Integer estimatedDurationMinutes;
    private CourseStatus status;
    private UUID createdById;
    private List<CourseModuleDto> modules = new ArrayList<>();
    private long totalLessonsCount;
    private Instant createdAt;

    public CourseDetailDto() {}

    public static CourseDetailDto fromEntity(Course entity, boolean publishedOnly) {
        if (entity == null) return null;
        CourseDetailDto dto = new CourseDetailDto();
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
        dto.setDescription(entity.getDescription());
        dto.setDescriptionTa(entity.getDescriptionTa());
        dto.setDifficulty(entity.getDifficulty());
        dto.setEstimatedDurationMinutes(entity.getEstimatedDurationMinutes());
        dto.setStatus(entity.getStatus());
        if (entity.getCreatedBy() != null) dto.setCreatedById(entity.getCreatedBy().getId());
        if (entity.getModules() != null) {
            dto.setModules(entity.getModules().stream()
                    .map(m -> CourseModuleDto.fromEntity(m, publishedOnly))
                    .collect(Collectors.toList()));
        }
        long lessonCount = dto.getModules().stream()
                .mapToLong(m -> m.getLessons().size())
                .sum();
        dto.setTotalLessonsCount(lessonCount);
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public CourseDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(CourseDifficulty difficulty) { this.difficulty = difficulty; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }

    public List<CourseModuleDto> getModules() { return modules; }
    public void setModules(List<CourseModuleDto> modules) { this.modules = modules; }

    public long getTotalLessonsCount() { return totalLessonsCount; }
    public void setTotalLessonsCount(long totalLessonsCount) { this.totalLessonsCount = totalLessonsCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
