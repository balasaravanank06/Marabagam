package com.marabagam.movement.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "movement_versions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"movement_id", "version_number"})
})
public class MovementVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movement_id", nullable = false)
    private Movement movement;

    @Column(name = "version_number", nullable = false, length = 20)
    private String versionNumber;

    @Column(name = "joint_rules_json", nullable = false, columnDefinition = "TEXT")
    private String jointRulesJson;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Movement getMovement() { return movement; }
    public void setMovement(Movement movement) { this.movement = movement; }

    public String getVersionNumber() { return versionNumber; }
    public void setVersionNumber(String versionNumber) { this.versionNumber = versionNumber; }

    public String getJointRulesJson() { return jointRulesJson; }
    public void setJointRulesJson(String jointRulesJson) { this.jointRulesJson = jointRulesJson; }

    public Boolean getIsCurrent() { return isCurrent; }
    public void setIsCurrent(Boolean isCurrent) { this.isCurrent = isCurrent; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
