package com.marabagam.movement.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.movement.dto.EvaluatePracticeSessionRequestDto;
import com.marabagam.movement.dto.PracticeSessionResponseDto;
import com.marabagam.movement.dto.StartPracticeSessionRequestDto;
import com.marabagam.movement.service.MovementService;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice-sessions")
@PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
public class PracticeSessionController {

    private final MovementService movementService;

    public PracticeSessionController(MovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PracticeSessionResponseDto>> startSession(
            @Valid @RequestBody StartPracticeSessionRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        PracticeSessionResponseDto result = movementService.startPracticeSession(request.getMovementId(), currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Practice session started", result));
    }

    @PostMapping("/{id}/evaluate")
    public ResponseEntity<ApiResponse<PracticeSessionResponseDto>> evaluateSession(
            @PathVariable UUID id,
            @Valid @RequestBody EvaluatePracticeSessionRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        PracticeSessionResponseDto result = movementService.evaluatePracticeSession(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Practice session evaluated successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PracticeSessionResponseDto>>> getUserSessions(
            Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        Page<PracticeSessionResponseDto> result = movementService.getUserPracticeSessions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("User practice session history fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PracticeSessionResponseDto>> getSessionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        PracticeSessionResponseDto result = movementService.getPracticeSessionById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Practice session retrieved successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        movementService.deletePracticeSession(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Practice session deleted successfully", null));
    }
}
