package com.marabagam.learning.dto;

import java.time.Instant;
import java.util.List;

public class RecommendationFeedDto {

    private List<RecommendationDto> recommendations;
    private Instant generatedAt;
    private Boolean isColdStart;

    public RecommendationFeedDto() {
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }

    public List<RecommendationDto> getItems() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Boolean getIsColdStart() {
        return isColdStart;
    }

    public void setIsColdStart(Boolean isColdStart) {
        this.isColdStart = isColdStart;
    }
}
