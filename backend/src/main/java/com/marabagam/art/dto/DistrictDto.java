package com.marabagam.art.dto;

public class DistrictDto {
    private Long id;
    private String name;
    private String nameTa;
    private String state;

    private String code;
    private Double latitude;
    private Double longitude;
    private String shortHistory;
    private String shortHistoryTa;
    private String culturalSignificance;
    private String culturalSignificanceTa;
    private String geojsonFeatureCode;
    private long artFormCount;
    private long locationCount;
    private long eventCount;

    public DistrictDto() {
    }

    public DistrictDto(Long id, String name, String nameTa, String state) {
        this.id = id;
        this.name = name;
        this.nameTa = nameTa;
        this.state = state;
    }

    public DistrictDto(Long id, String name, String nameTa, String code, Double latitude, Double longitude, String shortHistory, String shortHistoryTa, String culturalSignificance, String culturalSignificanceTa, String geojsonFeatureCode, long artFormCount, long locationCount, long eventCount) {
        this.id = id;
        this.name = name;
        this.nameTa = nameTa;
        this.state = "Tamil Nadu";
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shortHistory = shortHistory;
        this.shortHistoryTa = shortHistoryTa;
        this.culturalSignificance = culturalSignificance;
        this.culturalSignificanceTa = culturalSignificanceTa;
        this.geojsonFeatureCode = geojsonFeatureCode;
        this.artFormCount = artFormCount;
        this.locationCount = locationCount;
        this.eventCount = eventCount;
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

    public long getArtFormCount() {
        return artFormCount;
    }

    public void setArtFormCount(long artFormCount) {
        this.artFormCount = artFormCount;
    }

    public long getLocationCount() {
        return locationCount;
    }

    public void setLocationCount(long locationCount) {
        this.locationCount = locationCount;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }
}
