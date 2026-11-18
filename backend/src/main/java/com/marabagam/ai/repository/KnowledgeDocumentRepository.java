package com.marabagam.ai.repository;

import com.marabagam.ai.domain.DocumentStatus;
import com.marabagam.ai.domain.IndexingStatus;
import com.marabagam.ai.domain.KnowledgeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, UUID> {

    Page<KnowledgeDocument> findByCreatedById(UUID createdById, Pageable pageable);

    Page<KnowledgeDocument> findByStatus(DocumentStatus status, Pageable pageable);

    List<KnowledgeDocument> findByStatusAndIndexingStatus(DocumentStatus status, IndexingStatus indexingStatus);

    @Query("SELECT d FROM KnowledgeDocument d WHERE " +
           "(:search IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(d.content) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<KnowledgeDocument> searchDocuments(@Param("search") String search, @Param("status") DocumentStatus status, Pageable pageable);
}
