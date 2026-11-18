package com.marabagam.art.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "art_forms")
public class ArtForm {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "name_ta", nullable = false, length = 150)
    private String nameTa;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String shortDescription;

    @Column(name = "short_description_ta", nullable = false, columnDefinition = "TEXT")
    private String shortDescriptionTa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ta", nullable = false, columnDefinition = "TEXT")
    private String descriptionTa;

    @Column(length = 150)
    private String origin;

    @Column(name = "origin_ta", length = 150)
    private String originTa;

    @Column(name = "historical_period", length = 100)
    private String historicalPeriod;

    @Column(name = "historical_period_ta", length = 100)
    private String historicalPeriodTa;

    @Column(name = "cultural_significance", columnDefinition = "TEXT")
    private String culturalSignificance;

    @Column(name = "cultural_significance_ta", columnDefinition = "TEXT")
    private String culturalSignificanceTa;

    @Column(name = "performance_style", columnDefinition = "TEXT")
    private String performanceStyle;

    @Column(name = "performance_style_ta", columnDefinition = "TEXT")
    private String performanceStyleTa;

    @Column(columnDefinition = "TEXT")
    private String costume;

    @Column(name = "costume_ta", columnDefinition = "TEXT")
    private String costumeTa;

    @Column(columnDefinition = "TEXT")
    private String instruments;

    @Column(name = "instruments_ta", columnDefinition = "TEXT")
    private String instrumentsTa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ArtFormStatus status = ArtFormStatus.DRAFT;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "art_form_categories",
        joinColumns = @JoinColumn(name = "art_form_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "art_form_districts",
        joinColumns = @JoinColumn(name = "art_form_id"),
        inverseJoinColumns = @JoinColumn(name = "district_id")
    )
    private Set<District> districts = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ArtForm() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void setCategory(Category cat) {
        if (this.categories == null) this.categories = new HashSet<>();
        if (cat != null) this.categories.add(cat);
    }

    public void setOriginDistrict(District dist) {
        if (this.districts == null) this.districts = new HashSet<>();
        if (dist != null) this.districts.add(dist);
    }

    public UUID getId() {
        return id;
    }


    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameTa() {
        return nameTa;
    }

    public void setNameTa(String nameTa) {
        this.nameTa = nameTa;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getShortDescriptionTa() {
        return shortDescriptionTa;
    }

    public void setShortDescriptionTa(String shortDescriptionTa) {
        this.shortDescriptionTa = shortDescriptionTa;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionTa() {
        return descriptionTa;
    }

    public void setDescriptionTa(String descriptionTa) {
        this.descriptionTa = descriptionTa;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginTa() {
        return originTa;
    }

    public void setOriginTa(String originTa) {
        this.originTa = originTa;
    }

    public String getHistoricalPeriod() {
        return historicalPeriod;
    }

    public void setHistoricalPeriod(String historicalPeriod) {
        this.historicalPeriod = historicalPeriod;
    }

    public String getHistoricalPeriodTa() {
        return historicalPeriodTa;
    }

    public void setHistoricalPeriodTa(String historicalPeriodTa) {
        this.historicalPeriodTa = historicalPeriodTa;
    }

    public String getCulturalSignificance() {
        return culturalSignificance;
    }

    public void setCulturalSignificance(String culturalSignificance) {
        this.culturalSignificance = culturalSignificance;
    }

    public String getCulturalSignificanceTa() {
        return culturalSignificanceTa;
    }

    public void setCulturalSignificanceTa(String culturalSignificanceTa) {
        this.culturalSignificanceTa = culturalSignificanceTa;
    }

    public String getPerformanceStyle() {
        return performanceStyle;
    }

    public void setPerformanceStyle(String performanceStyle) {
        this.performanceStyle = performanceStyle;
    }

    public String getPerformanceStyleTa() {
        return performanceStyleTa;
    }

    public void setPerformanceStyleTa(String performanceStyleTa) {
        this.performanceStyleTa = performanceStyleTa;
    }

    public String getCostume() {
        return costume;
    }

    public void setCostume(String costume) {
        this.costume = costume;
    }

    public String getCostumeTa() {
        return costumeTa;
    }

    public void setCostumeTa(String costumeTa) {
        this.costumeTa = costumeTa;
    }

    public String getInstruments() {
        return instruments;
    }

    public void setInstruments(String instruments) {
        this.instruments = instruments;
    }

    public String getInstrumentsTa() {
        return instrumentsTa;
    }

    public void setInstrumentsTa(String instrumentsTa) {
        this.instrumentsTa = instrumentsTa;
    }

    public ArtFormStatus getStatus() {
        return status;
    }

    public void setStatus(ArtFormStatus status) {
        this.status = status;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<District> getDistricts() {
        return districts;
    }

    public void setDistricts(Set<District> districts) {
        this.districts = districts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
