package com.marabagam.art.dto;

public class CategoryDto {
    private Long id;
    private String name;
    private String nameTa;
    private String description;
    private String descriptionTa;

    public CategoryDto() {
    }

    public CategoryDto(Long id, String name, String nameTa, String description, String descriptionTa) {
        this.id = id;
        this.name = name;
        this.nameTa = nameTa;
        this.description = description;
        this.descriptionTa = descriptionTa;
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
}
