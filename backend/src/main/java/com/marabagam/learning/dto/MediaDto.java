package com.marabagam.learning.dto;

import com.marabagam.learning.domain.Media;
import com.marabagam.learning.domain.MediaType;

import java.time.Instant;
import java.util.UUID;

public class MediaDto {
    private UUID id;
    private UUID lessonId;
    private MediaType mediaType;
    private String title;
    private String titleTa;
    private String url;
    private String thumbnailUrl;
    private String mimeType;
    private Long fileSizeBytes;
    private Integer durationSeconds;
    private Integer displayOrder;
    private String altText;
    private String altTextTa;
    private Instant createdAt;

    public MediaDto() {}

    public static MediaDto fromEntity(Media entity) {
        if (entity == null) return null;
        MediaDto dto = new MediaDto();
        dto.setId(entity.getId());
        if (entity.getLesson() != null) dto.setLessonId(entity.getLesson().getId());
        dto.setMediaType(entity.getMediaType());
        dto.setTitle(entity.getTitle());
        dto.setTitleTa(entity.getTitleTa());
        dto.setUrl(entity.getUrl());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setMimeType(entity.getMimeType());
        dto.setFileSizeBytes(entity.getFileSizeBytes());
        dto.setDurationSeconds(entity.getDurationSeconds());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setAltText(entity.getAltText());
        dto.setAltTextTa(entity.getAltTextTa());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLessonId() { return lessonId; }
    public void setLessonId(UUID lessonId) { this.lessonId = lessonId; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }

    public String getAltTextTa() { return altTextTa; }
    public void setAltTextTa(String altTextTa) { this.altTextTa = altTextTa; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
