package com.marabagam.art.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public class CreateHeritageLocationRequestDto {

    @NotBlank(message = "Location name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Tamil location name is required")
    @Size(max = 200, message = "Tamil name must not exceed 200 characters")
    private String nameTa;

    @NotNull(message = "District ID is required")
    private Long districtId;

    private UUID artFormId;

    @NotBlank(message = "Location type is required")
    private String locationType;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Tamil description is required")
    private String descriptionTa;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Tamil address is required")
    private String addressTa;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String contactPhone;
    private String contactEmail;
    private String websiteUrl;
    private String provenanceSource;

    public CreateHeritageLocationRequestDto() {
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

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public UUID getArtFormId() {
        return artFormId;
    }

    public void setArtFormId(UUID artFormId) {
        this.artFormId = artFormId;
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
}
