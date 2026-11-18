package com.marabagam.art.dto;

import java.util.List;

public class HeritageMapSummaryDto {
    private String state = "Tamil Nadu";
    private int totalDistricts;
    private long totalPublishedArtForms;
    private long totalPublishedLocations;
    private long totalPublishedEvents;
    private List<DistrictDto> districts;

    public HeritageMapSummaryDto() {
    }

    public HeritageMapSummaryDto(String state, int totalDistricts, long totalPublishedArtForms, long totalPublishedLocations, long totalPublishedEvents, List<DistrictDto> districts) {
        this.state = state;
        this.totalDistricts = totalDistricts;
        this.totalPublishedArtForms = totalPublishedArtForms;
        this.totalPublishedLocations = totalPublishedLocations;
        this.totalPublishedEvents = totalPublishedEvents;
        this.districts = districts;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getTotalDistricts() {
        return totalDistricts;
    }

    public void setTotalDistricts(int totalDistricts) {
        this.totalDistricts = totalDistricts;
    }

    public long getTotalPublishedArtForms() {
        return totalPublishedArtForms;
    }

    public void setTotalPublishedArtForms(long totalPublishedArtForms) {
        this.totalPublishedArtForms = totalPublishedArtForms;
    }

    public long getTotalPublishedLocations() {
        return totalPublishedLocations;
    }

    public void setTotalPublishedLocations(long totalPublishedLocations) {
        this.totalPublishedLocations = totalPublishedLocations;
    }

    public long getTotalPublishedEvents() {
        return totalPublishedEvents;
    }

    public void setTotalPublishedEvents(long totalPublishedEvents) {
        this.totalPublishedEvents = totalPublishedEvents;
    }

    public List<DistrictDto> getDistricts() {
        return districts;
    }

    public void setDistricts(List<DistrictDto> districts) {
        this.districts = districts;
    }
}
