package com.marabagam.art.dto;

import java.util.Set;

public class UpdateArtFormRequestDto {
    private String name;
    private String nameTa;
    private String slug;
    private String shortDescription;
    private String shortDescriptionTa;
    private String description;
    private String descriptionTa;
    private String origin;
    private String originTa;
    private String historicalPeriod;
    private String historicalPeriodTa;
    private String culturalSignificance;
    private String culturalSignificanceTa;
    private String performanceStyle;
    private String performanceStyleTa;
    private String costume;
    private String costumeTa;
    private String instruments;
    private String instrumentsTa;

    private Set<Long> categoryIds;
    private Set<Long> districtIds;

    public UpdateArtFormRequestDto() {
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

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public Set<Long> getDistrictIds() {
        return districtIds;
    }

    public void setDistrictIds(Set<Long> districtIds) {
        this.districtIds = districtIds;
    }
}
