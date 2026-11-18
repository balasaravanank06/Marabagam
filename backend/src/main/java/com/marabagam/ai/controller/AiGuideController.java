package com.marabagam.ai.controller;

import com.marabagam.ai.dto.AiConversationDto;
import com.marabagam.ai.dto.AiGuideResponseDto;
import com.marabagam.ai.dto.AskCulturalGuideRequestDto;
import com.marabagam.ai.service.AiGuideService;
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
@RequestMapping("/api/v1/ai")
public class AiGuideController {

    private final AiGuideService aiGuideService;

    public AiGuideController(AiGuideService aiGuideService) {
        this.aiGuideService = aiGuideService;
    }

    @PostMapping("/cultural-guide")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiGuideResponseDto>> askCulturalGuide(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AskCulturalGuideRequestDto req) {
        AiGuideResponseDto response = aiGuideService.askCulturalGuide(req, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("AI Cultural Guide response generated", response));
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<AiConversationDto>>> getUserConversations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AiConversationDto> conversations = aiGuideService.getUserConversations(currentUser.getId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("User conversations fetched successfully", conversations));
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AiConversationDto>> getConversationById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        AiConversationDto conversation = aiGuideService.getConversationById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation fetched successfully", conversation));
    }

    @DeleteMapping("/conversations/{id}")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        aiGuideService.deleteConversation(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted successfully", null));
    }
}
