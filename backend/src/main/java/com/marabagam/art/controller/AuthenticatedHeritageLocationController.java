package com.marabagam.art.controller;

import com.marabagam.art.dto.CreateHeritageLocationRequestDto;
import com.marabagam.art.dto.HeritageLocationDto;
import com.marabagam.art.service.HeritageLocationService;
import com.marabagam.common.response.ApiResponse;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/heritage-locations")
public class AuthenticatedHeritageLocationController {

    @Autowired
    private HeritageLocationService heritageLocationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<HeritageLocationDto>> createLocation(
            @Valid @RequestBody CreateHeritageLocationRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        HeritageLocationDto created = heritageLocationService.createLocation(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Heritage location draft created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<HeritageLocationDto>> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody CreateHeritageLocationRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        HeritageLocationDto updated = heritageLocationService.updateLocation(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Heritage location updated successfully", updated));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<HeritageLocationDto>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User currentUser
    ) {
        String newStatus = payload.get("status");
        HeritageLocationDto updated = heritageLocationService.updateStatus(id, newStatus, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Heritage location status updated to " + newStatus, updated));
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> archiveLocation(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        heritageLocationService.archiveLocation(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Heritage location archived successfully", "ARCHIVED"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteLocation(@PathVariable UUID id) {
        heritageLocationService.deleteLocation(id);
        return ResponseEntity.ok(ApiResponse.success("Heritage location deleted successfully", "DELETED"));
    }
}
