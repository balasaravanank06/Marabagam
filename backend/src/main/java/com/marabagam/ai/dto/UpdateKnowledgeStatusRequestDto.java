package com.marabagam.ai.dto;

import com.marabagam.ai.domain.DocumentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateKnowledgeStatusRequestDto {

    @NotNull(message = "Document status is required")
    private DocumentStatus status;

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }
}
