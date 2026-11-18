package com.marabagam.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AiMessageDto {
    private UUID id;
    private String sender;
    private String content;
    private List<SourceAttributionDto> sources;
    private Double relevanceScore;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<SourceAttributionDto> getSources() { return sources; }
    public void setSources(List<SourceAttributionDto> sources) { this.sources = sources; }

    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
