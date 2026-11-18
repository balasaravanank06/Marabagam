package com.marabagam.art.dto;

import java.util.List;

public class DistrictDetailDto {
    private Long id;
    private String name;
    private String nameTa;
    private String state = "Tamil Nadu";
    private String code;
    private Double latitude;
    private Double longitude;
    private String shortHistory;
    private String shortHistoryTa;
    private String culturalSignificance;
    private String culturalSignificanceTa;
    private String geojsonFeatureCode;
    private List<ArtFormSummaryDto> artForms;
    private List<HeritageLocationDto> locations;
    private List<CulturalEventDto> events;

    public DistrictDetailDto() {
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public List<ArtFormSummaryDto> getArtForms() {
        return artForms;
    }

    public void setArtForms(List<ArtFormSummaryDto> artForms) {
        this.artForms = artForms;
    }

    public List<HeritageLocationDto> getLocations() {
        return locations;
    }

    public void setLocations(List<HeritageLocationDto> locations) {
        this.locations = locations;
    }

    public List<CulturalEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<CulturalEventDto> events) {
        this.events = events;
    }
}
