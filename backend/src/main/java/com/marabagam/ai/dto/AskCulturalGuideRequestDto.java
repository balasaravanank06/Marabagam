package com.marabagam.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class AskCulturalGuideRequestDto {

    private UUID conversationId;

    @NotBlank(message = "Query text is required")
    @Size(max = 1000, message = "Query text cannot exceed 1000 characters")
    private String query;

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
