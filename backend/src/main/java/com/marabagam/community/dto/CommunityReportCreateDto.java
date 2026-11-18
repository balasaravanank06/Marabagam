package com.marabagam.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CommunityReportCreateDto {

    @NotBlank(message = "Target type is required")
    private String targetType; // 'POST', 'COMMENT'

    @NotNull(message = "Target ID is required")
    private UUID targetId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String details;

    public CommunityReportCreateDto() {
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
}
