package com.marabagam.community.dto;

import jakarta.validation.constraints.Size;

public class CommunityPostUpdateDto {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;

    public CommunityPostUpdateDto() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
