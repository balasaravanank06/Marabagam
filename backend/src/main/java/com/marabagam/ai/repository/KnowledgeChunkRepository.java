package com.marabagam.ai.repository;

import com.marabagam.ai.domain.KnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, UUID> {
    List<KnowledgeChunk> findByDocumentId(UUID documentId);
    void deleteByDocumentId(UUID documentId);
}
