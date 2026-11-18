package com.marabagam.learning.dto;

import com.marabagam.learning.domain.LessonStatus;
import com.marabagam.learning.domain.LessonType;
import com.marabagam.learning.domain.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class CreateLessonRequestDto {

    @NotBlank(message = "Lesson title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotBlank(message = "Tamil title is required")
    @Size(max = 255, message = "Tamil title must not exceed 255 characters")
    private String titleTa;

    private String description;
    private String descriptionTa;

    private String content;
    private String contentTa;

    @NotNull(message = "Lesson type is required")
    private LessonType lessonType = LessonType.TEXT;

    private Integer estimatedDurationMinutes = 0;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    private LessonStatus status = LessonStatus.DRAFT;

    private List<MediaItemRequestDto> mediaItems = new ArrayList<>();

    public CreateLessonRequestDto() {}

    public static class MediaItemRequestDto {
        @NotNull(message = "Media type is required")
        private MediaType mediaType;

        @NotBlank(message = "Media title is required")
        private String title;
        private String titleTa;

        @NotBlank(message = "Media URL is required")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        private String url;

        @Size(max = 2048, message = "Thumbnail URL must not exceed 2048 characters")
        private String thumbnailUrl;

        private String mimeType;
        private Long fileSizeBytes;
        private Integer durationSeconds;
        private Integer displayOrder = 1;
        private String altText;
        private String altTextTa;

        public MediaItemRequestDto() {}

        // Getters and Setters
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
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDescriptionTa() { return descriptionTa; }
    public void setDescriptionTa(String descriptionTa) { this.descriptionTa = descriptionTa; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentTa() { return contentTa; }
    public void setContentTa(String contentTa) { this.contentTa = contentTa; }

    public LessonType getLessonType() { return lessonType; }
    public void setLessonType(LessonType lessonType) { this.lessonType = lessonType; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LessonStatus getStatus() { return status; }
    public void setStatus(LessonStatus status) { this.status = status; }

    public List<MediaItemRequestDto> getMediaItems() { return mediaItems; }
    public void setMediaItems(List<MediaItemRequestDto> mediaItems) { this.mediaItems = mediaItems; }
}
