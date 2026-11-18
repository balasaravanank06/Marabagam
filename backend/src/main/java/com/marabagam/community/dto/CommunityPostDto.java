package com.marabagam.community.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CommunityPostDto {

    private UUID id;
    private String authorDisplayName;
    private String authorRole;
    private String authorAvatarUrl;
    private UUID artFormId;
    private String artFormName;
    private String category;
    private String title;
    private String content;
    private String status;
    private long commentCount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommunityCommentDto> comments;

    public CommunityPostDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public void setAuthorAvatarUrl(String authorAvatarUrl) {
        this.authorAvatarUrl = authorAvatarUrl;
    }

    public UUID getArtFormId() {
        return artFormId;
    }

    public void setArtFormId(UUID artFormId) {
        this.artFormId = artFormId;
    }

    public String getArtFormName() {
        return artFormName;
    }

    public void setArtFormName(String artFormName) {
        this.artFormName = artFormName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<CommunityCommentDto> getComments() {
        return comments;
    }

    public void setComments(List<CommunityCommentDto> comments) {
        this.comments = comments;
    }
}
