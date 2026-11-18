package com.marabagam.learning.dto;

import com.marabagam.learning.domain.CourseDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateCourseRequestDto {

    @NotBlank(message = "Course title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Tamil title is required")
    @Size(max = 255, message = "Tamil title must not exceed 255 characters")
    private String titleTa;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @NotBlank(message = "Short description is required")
    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @NotBlank(message = "Tamil short description is required")
    @Size(max = 500, message = "Tamil short description must not exceed 500 characters")
    private String shortDescriptionTa;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Tamil description is required")
    private String descriptionTa;

    @NotNull(message = "Difficulty level is required")
    private CourseDifficulty difficulty;

    private Integer estimatedDurationMinutes = 0;

    public UpdateCourseRequestDto() {}

    // Getters and Setters
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
}
