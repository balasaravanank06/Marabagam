package com.marabagam.gamification.dto;

import java.time.Instant;
import java.util.UUID;

public class RecentActivityDto {
    private UUID id;
    private Integer amount;
    private String sourceActivity;
    private Instant createdAt;

    public RecentActivityDto() {
    }

    public RecentActivityDto(UUID id, Integer amount, String sourceActivity, Instant createdAt) {
        this.id = id;
        this.amount = amount;
        this.sourceActivity = sourceActivity;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getSourceActivity() {
        return sourceActivity;
    }

    public void setSourceActivity(String sourceActivity) {
        this.sourceActivity = sourceActivity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
