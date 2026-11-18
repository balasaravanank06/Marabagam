package com.marabagam.ai.controller;

import com.marabagam.ai.domain.DocumentStatus;
import com.marabagam.ai.dto.CreateKnowledgeDocumentRequestDto;
import com.marabagam.ai.dto.KnowledgeDocumentDto;
import com.marabagam.ai.dto.UpdateKnowledgeStatusRequestDto;
import com.marabagam.ai.service.KnowledgeDocumentService;
import com.marabagam.common.response.ApiResponse;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/knowledge-documents")
public class AdminKnowledgeController {

    private final KnowledgeDocumentService knowledgeDocumentService;

    public AdminKnowledgeController(KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> createDocument(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateKnowledgeDocumentRequestDto dto) {
        KnowledgeDocumentDto created = knowledgeDocumentService.createDocument(dto, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Knowledge document created", created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<KnowledgeDocumentDto>>> getDocuments(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<KnowledgeDocumentDto> docs = knowledgeDocumentService.getDocuments(search, status, currentUser.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Knowledge documents fetched successfully", docs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> getDocumentById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        KnowledgeDocumentDto doc = knowledgeDocumentService.getDocumentById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Knowledge document fetched successfully", doc));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<KnowledgeDocumentDto>> updateDocumentStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateKnowledgeStatusRequestDto dto) {
        KnowledgeDocumentDto updated = knowledgeDocumentService.updateDocumentStatus(id, dto.getStatus(), currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Document status updated", updated));
    }
}
