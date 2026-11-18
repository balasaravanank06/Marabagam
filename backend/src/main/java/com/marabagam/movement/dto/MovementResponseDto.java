package com.marabagam.movement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MovementResponseDto {

    private UUID id;
    private UUID artFormId;
    private String artFormName;
    private String code;
    private String nameEn;
    private String nameTa;
    private String descriptionEn;
    private String descriptionTa;
    private String category;
    private String difficultyLevel;
    private Boolean isActive;
    private String currentVersionNumber;
    private String demonstrationVideoUrl;
    private String thumbnailUrl;
    private OffsetDateTime createdAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getArtFormId() { return artFormId; }
    public void setArtFormId(UUID artFormId) { this.artFormId = artFormId; }

    public String getArtFormName() { return artFormName; }
    public void setArtFormName(String artFormName) { this.artFormName = artFormName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameTa() { return nameTa; }
    public void setNameTa(String nameTa) { this.nameTa = nameTa; }

    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getCurrentVersionNumber() { return currentVersionNumber; }
    public void setCurrentVersionNumber(String currentVersionNumber) { this.currentVersionNumber = currentVersionNumber; }

    public String getDemonstrationVideoUrl() { return demonstrationVideoUrl; }
    public void setDemonstrationVideoUrl(String demonstrationVideoUrl) { this.demonstrationVideoUrl = demonstrationVideoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
