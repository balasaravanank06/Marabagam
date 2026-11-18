package com.marabagam.ai.dto;

import com.marabagam.ai.domain.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CreateKnowledgeDocumentRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    private String titleTa;

    @NotBlank(message = "Content is required")
    private String content;

    private String contentTa;

    @NotNull(message = "Source type is required")
    private SourceType sourceType;

    @NotBlank(message = "Source name is required")
    private String sourceName;

    private String sourceUrl;
    private String author;
    private String language = "en";
    private UUID artFormId;
    private Long districtId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleTa() { return titleTa; }
    public void setTitleTa(String titleTa) { this.titleTa = titleTa; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContentTa() { return contentTa; }
    public void setContentTa(String contentTa) { this.contentTa = contentTa; }

    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public UUID getArtFormId() { return artFormId; }
    public void setArtFormId(UUID artFormId) { this.artFormId = artFormId; }

    public Long getDistrictId() { return districtId; }
    public void setDistrictId(Long districtId) { this.districtId = districtId; }
}
