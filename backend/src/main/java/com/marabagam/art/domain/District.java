package com.marabagam.art.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "name_ta", nullable = false, length = 100)
    private String nameTa;

    @Column(unique = true, length = 50)
    private String code;

    @Column(columnDefinition = "NUMERIC(8, 5)")
    private Double latitude;

    @Column(columnDefinition = "NUMERIC(8, 5)")
    private Double longitude;

    @Column(name = "short_history", columnDefinition = "TEXT")
    private String shortHistory;

    @Column(name = "short_history_ta", columnDefinition = "TEXT")
    private String shortHistoryTa;

    @Column(name = "cultural_significance", columnDefinition = "TEXT")
    private String culturalSignificance;

    @Column(name = "cultural_significance_ta", columnDefinition = "TEXT")
    private String culturalSignificanceTa;

    @Column(name = "geojson_feature_code", length = 50)
    private String geojsonFeatureCode;

    @Column(nullable = false, length = 100)
    private String state = "Tamil Nadu";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public District() {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getShortHistory() {
        return shortHistory;
    }

    public void setShortHistory(String shortHistory) {
        this.shortHistory = shortHistory;
    }

    public String getShortHistoryTa() {
        return shortHistoryTa;
    }

    public void setShortHistoryTa(String shortHistoryTa) {
        this.shortHistoryTa = shortHistoryTa;
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

    public String getGeojsonFeatureCode() {
        return geojsonFeatureCode;
    }

    public void setGeojsonFeatureCode(String geojsonFeatureCode) {
        this.geojsonFeatureCode = geojsonFeatureCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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
