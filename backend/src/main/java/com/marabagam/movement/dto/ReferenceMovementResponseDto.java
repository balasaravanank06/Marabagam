package com.marabagam.movement.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ReferenceMovementResponseDto {

    private UUID id;
    private UUID movementId;
    private String movementNameEn;
    private String versionNumber;
    private String title;
    private String performerAttribution;
    private String demonstrationVideoUrl;
    private String thumbnailUrl;
    private String status;
    private UUID createdById;
    private String createdByName;
    private UUID approvedById;
    private String approvedByName;
    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMovementId() { return movementId; }
    public void setMovementId(UUID movementId) { this.movementId = movementId; }

    public String getMovementNameEn() { return movementNameEn; }
    public void setMovementNameEn(String movementNameEn) { this.movementNameEn = movementNameEn; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPerformerAttribution() { return performerAttribution; }
    public void setPerformerAttribution(String performerAttribution) { this.performerAttribution = performerAttribution; }

    public String getDemonstrationVideoUrl() { return demonstrationVideoUrl; }
    public void setDemonstrationVideoUrl(String demonstrationVideoUrl) { this.demonstrationVideoUrl = demonstrationVideoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UUID getCreatedById() { return createdById; }
    public void setCreatedById(UUID createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public UUID getApprovedById() { return approvedById; }
    public void setApprovedById(UUID approvedById) { this.approvedById = approvedById; }

    public String getApprovedByName() { return approvedByName; }
    public void setApprovedByName(String approvedByName) { this.approvedByName = approvedByName; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
