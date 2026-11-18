package com.marabagam.ai.domain;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.District;
import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "title_ta")
    private String titleTa;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_ta", columnDefinition = "TEXT")
    private String contentTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "source_url", length = 2048)
    private String sourceUrl;

    private String author;

    @Column(nullable = false, length = 10)
    private String language = "en";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_form_id")
    private ArtForm artForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "indexing_status", nullable = false)
    private IndexingStatus indexingStatus = IndexingStatus.NOT_INDEXED;

    @Column(name = "embedding_model")
    private String embeddingModel = "text-embedding-3-small";

    @Column(name = "embedding_dimension")
    private Integer embeddingDimension = 1536;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Getters and Setters
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

    public ArtForm getArtForm() { return artForm; }
    public void setArtForm(ArtForm artForm) { this.artForm = artForm; }

    public District getDistrict() { return district; }
    public void setDistrict(District district) { this.district = district; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public IndexingStatus getIndexingStatus() { return indexingStatus; }
    public void setIndexingStatus(IndexingStatus indexingStatus) { this.indexingStatus = indexingStatus; }

    public String getEmbeddingModel() { return embeddingModel; }
    public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }

    public Integer getEmbeddingDimension() { return embeddingDimension; }
    public void setEmbeddingDimension(Integer embeddingDimension) { this.embeddingDimension = embeddingDimension; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
