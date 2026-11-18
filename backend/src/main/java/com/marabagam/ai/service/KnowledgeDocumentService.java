package com.marabagam.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.ai.domain.*;
import com.marabagam.ai.dto.*;
import com.marabagam.ai.repository.KnowledgeChunkRepository;
import com.marabagam.ai.repository.KnowledgeDocumentRepository;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.common.exception.ForbiddenActionException;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KnowledgeDocumentService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocumentService.class);

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final ArtFormRepository artFormRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.service.url:http://ai-service:8000}")
    private String aiServiceUrl;

    public KnowledgeDocumentService(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            ArtFormRepository artFormRepository,
            DistrictRepository districtRepository,
            UserRepository userRepository,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.artFormRepository = artFormRepository;
        this.districtRepository = districtRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public KnowledgeDocumentDto createDocument(CreateKnowledgeDocumentRequestDto dto, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(dto.getTitle());
        doc.setTitleTa(dto.getTitleTa());
        doc.setContent(dto.getContent());
        doc.setContentTa(dto.getContentTa());
        doc.setSourceType(dto.getSourceType());
        doc.setSourceName(dto.getSourceName());
        doc.setSourceUrl(dto.getSourceUrl());
        doc.setAuthor(dto.getAuthor());
        doc.setLanguage(dto.getLanguage() != null ? dto.getLanguage() : "en");
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setIndexingStatus(IndexingStatus.NOT_INDEXED);
        doc.setCreatedBy(user);

        if (dto.getArtFormId() != null) {
            doc.setArtForm(artFormRepository.findById(dto.getArtFormId()).orElse(null));
        }
        if (dto.getDistrictId() != null) {
            doc.setDistrict(districtRepository.findById(dto.getDistrictId()).orElse(null));
        }

        KnowledgeDocument saved = documentRepository.save(doc);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<KnowledgeDocumentDto> getDocuments(String search, DocumentStatus status, UUID currentUserId, Pageable pageable) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (user.getRole() == Role.ARTIST || user.getRole() == Role.INSTRUCTOR) {
            return documentRepository.findByCreatedById(currentUserId, pageable).map(this::mapToDto);
        }

        return documentRepository.searchDocuments(search, status, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public KnowledgeDocumentDto getDocumentById(UUID id, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found with id: " + id));

        if ((user.getRole() == Role.ARTIST || user.getRole() == Role.INSTRUCTOR) && !doc.getCreatedBy().getId().equals(currentUserId)) {
            throw new ForbiddenActionException("Contributors can only view their own knowledge drafts");
        }

        return mapToDto(doc);
    }

    @Transactional
    public KnowledgeDocumentDto updateDocumentStatus(UUID id, DocumentStatus newStatus, UUID currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        KnowledgeDocument doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found with id: " + id));

        // Ownership and Governance Checks
        if (user.getRole() == Role.ARTIST || user.getRole() == Role.INSTRUCTOR) {
            if (!doc.getCreatedBy().getId().equals(currentUserId)) {
                throw new ForbiddenActionException("Contributors can only edit their own knowledge documents");
            }
            if (newStatus == DocumentStatus.APPROVED || newStatus == DocumentStatus.ARCHIVED) {
                throw new ForbiddenActionException("Contributors cannot directly approve or archive knowledge documents");
            }
        }

        if (user.getRole() == Role.CULTURAL_EXPERT) {
            if (newStatus == DocumentStatus.DRAFT) {
                throw new ForbiddenActionException("Cultural experts cannot set documents back to draft");
            }
        }

        doc.setStatus(newStatus);
        doc.setReviewedBy(user);

        // If newly approved, trigger indexing process
        if (newStatus == DocumentStatus.APPROVED) {
            triggerIndexingPipeline(doc);
        } else if (newStatus == DocumentStatus.ARCHIVED || newStatus == DocumentStatus.OUTDATED || newStatus == DocumentStatus.REJECTED) {
            // Purge vector chunks from retrieval index
            chunkRepository.deleteByDocumentId(doc.getId());
            doc.setIndexingStatus(IndexingStatus.NOT_INDEXED);
        }

        KnowledgeDocument updated = documentRepository.save(doc);
        return mapToDto(updated);
    }

    @Transactional
    public void triggerIndexingPipeline(KnowledgeDocument doc) {
        try {
            doc.setIndexingStatus(IndexingStatus.INDEXING);
            documentRepository.save(doc);

            // Call FastAPI internal index endpoint
            Map<String, Object> request = new HashMap<>();
            request.put("documentId", doc.getId().toString());
            request.put("title", doc.getTitle());
            request.put("content", doc.getContent());
            request.put("language", doc.getLanguage());
            request.put("author", doc.getAuthor() != null ? doc.getAuthor() : "Unknown");
            request.put("sourceName", doc.getSourceName());
            request.put("sourceUrl", doc.getSourceUrl());
            request.put("sourceType", doc.getSourceType().name());

            String url = aiServiceUrl + "/internal/v1/rag/index";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                doc.setIndexingStatus(IndexingStatus.INDEXED);
            } else {
                doc.setIndexingStatus(IndexingStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Failed to trigger indexing pipeline for document {}: {}", doc.getId(), e.getMessage());
            doc.setIndexingStatus(IndexingStatus.FAILED);
        }
        documentRepository.save(doc);
    }

    private KnowledgeDocumentDto mapToDto(KnowledgeDocument doc) {
        KnowledgeDocumentDto dto = new KnowledgeDocumentDto();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setTitleTa(doc.getTitleTa());
        dto.setContent(doc.getContent());
        dto.setContentTa(doc.getContentTa());
        dto.setSourceType(doc.getSourceType());
        dto.setSourceName(doc.getSourceName());
        dto.setSourceUrl(doc.getSourceUrl());
        dto.setAuthor(doc.getAuthor());
        dto.setLanguage(doc.getLanguage());
        dto.setStatus(doc.getStatus());
        dto.setIndexingStatus(doc.getIndexingStatus());
        dto.setEmbeddingModel(doc.getEmbeddingModel());
        dto.setEmbeddingDimension(doc.getEmbeddingDimension());
        dto.setCreatedByDisplayName(doc.getCreatedBy() != null ? doc.getCreatedBy().getDisplayName() : null);
        dto.setReviewedByDisplayName(doc.getReviewedBy() != null ? doc.getReviewedBy().getDisplayName() : null);
        dto.setVersion(doc.getVersion());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        return dto;
    }
}
