package com.marabagam.learning.domain;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "art_form_id", nullable = false)
    private ArtForm artForm;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "title_ta", nullable = false, length = 255)
    private String titleTa;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "short_description", nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "short_description_ta", nullable = false, length = 500)
    private String shortDescriptionTa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ta", nullable = false, columnDefinition = "TEXT")
    private String descriptionTa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseDifficulty difficulty = CourseDifficulty.BEGINNER;

    @Column(name = "estimated_duration_minutes", nullable = false)
    private Integer estimatedDurationMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CourseStatus status = CourseStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<CourseModule> modules = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Course() {}

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ArtForm getArtForm() { return artForm; }
    public void setArtForm(ArtForm artForm) { this.artForm = artForm; }

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

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<CourseModule> getModules() { return modules; }
    public void setModules(List<CourseModule> modules) { this.modules = modules; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
