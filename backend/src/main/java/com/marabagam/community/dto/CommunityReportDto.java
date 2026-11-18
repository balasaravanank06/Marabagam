package com.marabagam.community.dto;

import java.time.Instant;
import java.util.UUID;

public class CommunityReportDto {

    private UUID id;
    private String reporterName;
    private String targetType;
    private UUID targetId;
    private String targetContentSnippet;
    private String reason;
    private String details;
    private String status;
    private String actionTaken;
    private String actionedByName;
    private Instant actionedAt;
    private Instant createdAt;

    public CommunityReportDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public String getTargetContentSnippet() {
        return targetContentSnippet;
    }

    public void setTargetContentSnippet(String targetContentSnippet) {
        this.targetContentSnippet = targetContentSnippet;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getActionedByName() {
        return actionedByName;
    }

    public void setActionedByName(String actionedByName) {
        this.actionedByName = actionedByName;
    }

    public Instant getActionedAt() {
        return actionedAt;
    }

    public void setActionedAt(Instant actionedAt) {
        this.actionedAt = actionedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
