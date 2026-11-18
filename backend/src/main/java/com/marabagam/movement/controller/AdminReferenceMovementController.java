package com.marabagam.movement.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.movement.dto.ReferenceMovementResponseDto;
import com.marabagam.movement.dto.SubmitReferenceRequestDto;
import com.marabagam.movement.service.MovementService;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminReferenceMovementController {

    private final MovementService movementService;

    public AdminReferenceMovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping("/movements/{id}/references")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReferenceMovementResponseDto>> submitReference(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitReferenceRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        ReferenceMovementResponseDto result = movementService.submitReferenceMovement(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Reference movement submitted for review", result));
    }

    @PutMapping("/references/{id}/approve")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<ReferenceMovementResponseDto>> approveReference(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        ReferenceMovementResponseDto result = movementService.approveReferenceMovement(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Reference movement approved into production", result));
    }
}
