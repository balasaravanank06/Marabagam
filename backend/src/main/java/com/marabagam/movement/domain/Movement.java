package com.marabagam.movement.domain;

import com.marabagam.art.domain.ArtForm;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "movements")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "art_form_id", nullable = false)
    private ArtForm artForm;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_ta", nullable = false)
    private String nameTa;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_ta", columnDefinition = "TEXT")
    private String descriptionTa;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // STANCE, STRIKE, SWING, FOOTWORK

    @Column(name = "difficulty_level", nullable = false, length = 50)
    private String difficultyLevel = "BEGINNER";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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

    public ArtForm getArtForm() { return artForm; }
    public void setArtForm(ArtForm artForm) { this.artForm = artForm; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
