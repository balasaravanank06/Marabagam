package com.marabagam.ai.dto;

import com.marabagam.ai.domain.DocumentStatus;
import com.marabagam.ai.domain.IndexingStatus;
import com.marabagam.ai.domain.SourceType;
import java.time.Instant;
import java.util.UUID;

public class KnowledgeDocumentDto {
    private UUID id;
    private String title;
    private String titleTa;
    private String content;
    private String contentTa;
    private SourceType sourceType;
    private String sourceName;
    private String sourceUrl;
    private String author;
    private String language;
    private DocumentStatus status;
    private IndexingStatus indexingStatus;
    private String embeddingModel;
    private Integer embeddingDimension;
    private String createdByDisplayName;
    private String reviewedByDisplayName;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public IndexingStatus getIndexingStatus() { return indexingStatus; }
    public void setIndexingStatus(IndexingStatus indexingStatus) { this.indexingStatus = indexingStatus; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public Integer getEmbeddingDimension() { return embeddingDimension; }
    public void setEmbeddingDimension(Integer embeddingDimension) { this.embeddingDimension = embeddingDimension; }

    public String getCreatedByDisplayName() { return createdByDisplayName; }
    public void setCreatedByDisplayName(String createdByDisplayName) { this.createdByDisplayName = createdByDisplayName; }

    public String getReviewedByDisplayName() { return reviewedByDisplayName; }
    public void setReviewedByDisplayName(String reviewedByDisplayName) { this.reviewedByDisplayName = reviewedByDisplayName; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
