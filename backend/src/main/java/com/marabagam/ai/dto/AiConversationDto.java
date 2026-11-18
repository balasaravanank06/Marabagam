package com.marabagam.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AiConversationDto {
    private UUID id;
    private String title;
    private List<AiMessageDto> messages;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<AiMessageDto> getMessages() { return messages; }
    public void setMessages(List<AiMessageDto> messages) { this.messages = messages; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
