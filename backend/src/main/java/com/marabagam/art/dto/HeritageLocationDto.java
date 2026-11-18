package com.marabagam.art.dto;

import java.time.Instant;
import java.util.UUID;

public class HeritageLocationDto {
    private UUID id;
    private String name;
    private String nameTa;
    private String slug;
    private Long districtId;
    private String districtName;
    private String districtNameTa;
    private UUID artFormId;
    private String artFormName;
    private String artFormNameTa;
    private String locationType;
    private String description;
    private String descriptionTa;
    private String address;
    private String addressTa;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String contactEmail;
    private String websiteUrl;
    private String provenanceSource;
    private String status;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    public HeritageLocationDto() {
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

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictNameTa() {
        return districtNameTa;
    }

    public void setDistrictNameTa(String districtNameTa) {
        this.districtNameTa = districtNameTa;
    }

    public UUID getArtFormId() {
        return artFormId;
    }

    public void setArtFormId(UUID artFormId) {
        this.artFormId = artFormId;
    }

    public String getArtFormName() {
        return artFormName;
    }

    public void setArtFormName(String artFormName) {
        this.artFormName = artFormName;
    }

    public String getArtFormNameTa() {
        return artFormNameTa;
    }

    public void setArtFormNameTa(String artFormNameTa) {
        this.artFormNameTa = artFormNameTa;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressTa() {
        return addressTa;
    }

    public void setAddressTa(String addressTa) {
        this.addressTa = addressTa;
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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getProvenanceSource() {
        return provenanceSource;
    }

    public void setProvenanceSource(String provenanceSource) {
        this.provenanceSource = provenanceSource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
