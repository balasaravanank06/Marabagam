package com.marabagam.learning.dto;

import java.util.UUID;

public class RecommendationDto {

    private UUID id;
    private String itemType; // 'COURSE', 'EVENT', 'ART_FORM'
    private UUID itemId;
    private String title;
    private String titleTa;
    private String description;
    private String descriptionTa;
    private String imageUrl;
    private String targetUrl;
    private String artFormName;
    private String artFormNameTa;
    private String reason;
    private String reasonTa;
    private int rank;

    public RecommendationDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleTa() {
        return titleTa;
    }

    public void setTitleTa(String titleTa) {
        this.titleTa = titleTa;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonTa() {
        return reasonTa;
    }

    public void setReasonTa(String reasonTa) {
        this.reasonTa = reasonTa;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
