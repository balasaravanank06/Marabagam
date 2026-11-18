package com.marabagam.art.controller;

import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.dto.*;
import com.marabagam.art.service.ArtFormService;
import com.marabagam.common.response.ApiResponse;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/art-forms")
public class AdminArtFormController {

    @Autowired
    private ArtFormService artFormService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<ArtFormSummaryDto>>> getArtFormsForManagement(
            @RequestParam(required = false) ArtFormStatus status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<ArtFormSummaryDto> result = artFormService.getArtFormsForManagement(status, category, district, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Management art forms retrieved successfully", result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArtFormResponseDto>> getArtFormById(@PathVariable UUID id) {
        ArtFormResponseDto artForm = artFormService.getArtFormById(id);
        return ResponseEntity.ok(ApiResponse.success("Art form details retrieved successfully", artForm));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArtFormResponseDto>> createArtForm(
            @Valid @RequestBody CreateArtFormRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        ArtFormResponseDto created = artFormService.createArtForm(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Art form created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArtFormResponseDto>> updateArtForm(
            @PathVariable UUID id,
            @RequestBody UpdateArtFormRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        ArtFormResponseDto updated = artFormService.updateArtForm(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Art form updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArtFormResponseDto>> updateArtFormStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateArtFormStatusRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        ArtFormResponseDto updated = artFormService.updateArtFormStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Art form status updated successfully to " + request.getStatus(), updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteArtForm(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        artFormService.deleteArtForm(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Art form deleted successfully", "Record removed"));
    }
}
