package com.marabagam.movement.domain;

import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reference_movements")
public class ReferenceMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movement_id", nullable = false)
    private Movement movement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private MovementVersion version;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "performer_attribution", nullable = false)
    private String performerAttribution;

    @Column(name = "keypoints_template_json", nullable = false, columnDefinition = "TEXT")
    private String keypointsTemplateJson;

    @Column(name = "demonstration_video_url", length = 500)
    private String demonstrationVideoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ReferenceStatus status = ReferenceStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Movement getMovement() { return movement; }
    public void setMovement(Movement movement) { this.movement = movement; }

    public MovementVersion getVersion() { return version; }
    public void setVersion(MovementVersion version) { this.version = version; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPerformerAttribution() { return performerAttribution; }
    public void setPerformerAttribution(String performerAttribution) { this.performerAttribution = performerAttribution; }

    public String getKeypointsTemplateJson() { return keypointsTemplateJson; }
    public void setKeypointsTemplateJson(String keypointsTemplateJson) { this.keypointsTemplateJson = keypointsTemplateJson; }

    public String getDemonstrationVideoUrl() { return demonstrationVideoUrl; }
    public void setDemonstrationVideoUrl(String demonstrationVideoUrl) { this.demonstrationVideoUrl = demonstrationVideoUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public ReferenceStatus getStatus() { return status; }
    public void setStatus(ReferenceStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
