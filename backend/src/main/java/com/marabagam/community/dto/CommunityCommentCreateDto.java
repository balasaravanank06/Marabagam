package com.marabagam.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CommunityCommentCreateDto {

    private UUID parentCommentId;

    @NotBlank(message = "Comment content is required")
    @Size(max = 500, message = "Comment content cannot exceed 500 characters")
    private String content;

    public CommunityCommentCreateDto() {
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
