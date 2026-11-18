package com.marabagam.art.dto;

import com.marabagam.art.domain.ArtFormStatus;

import java.util.List;
import java.util.UUID;

public class ArtFormSummaryDto {
    private UUID id;
    private String name;
    private String nameTa;
    private String slug;
    private String shortDescription;
    private String shortDescriptionTa;
    private String origin;
    private String originTa;
    private ArtFormStatus status;
    private List<CategoryDto> categories;
    private List<DistrictDto> districts;

    public ArtFormSummaryDto() {
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

    public ArtFormStatus getStatus() {
        return status;
    }

    public void setStatus(ArtFormStatus status) {
        this.status = status;
    }

    public List<CategoryDto> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDto> categories) {
        this.categories = categories;
    }

    public List<DistrictDto> getDistricts() {
        return districts;
    }

    public void setDistricts(List<DistrictDto> districts) {
        this.districts = districts;
    }
}
