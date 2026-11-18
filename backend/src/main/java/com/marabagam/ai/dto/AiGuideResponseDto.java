package com.marabagam.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AiGuideResponseDto {
    private UUID conversationId;
    private UUID messageId;
    private String answer;
    private String language;
    private List<SourceAttributionDto> sources;
    private Double relevanceScore;
    private Instant createdAt;

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<SourceAttributionDto> getSources() { return sources; }
    public void setSources(List<SourceAttributionDto> sources) { this.sources = sources; }

    public Double getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Double relevanceScore) { this.relevanceScore = relevanceScore; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
