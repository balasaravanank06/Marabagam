package com.marabagam.community.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.community.dto.*;
import com.marabagam.community.service.CommunityService;
import com.marabagam.community.service.ModerationService;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
public class CommunityController {

    private final CommunityService communityService;
    private final ModerationService moderationService;

    public CommunityController(CommunityService communityService,
                               ModerationService moderationService) {
        this.communityService = communityService;
        this.moderationService = moderationService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<CommunityPostDto>>> getPublicPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID artFormId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityPostDto> posts = communityService.getPublicPosts(category, artFormId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Posts retrieved successfully", posts));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<CommunityPostDto>> getPostDetail(@PathVariable UUID id) {
        CommunityPostDto post = communityService.getPostDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Post detail retrieved successfully", post));
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityPostDto>> createPost(
            @Valid @RequestBody CommunityPostCreateDto createDto,
            @AuthenticationPrincipal User currentUser) {
        CommunityPostDto created = communityService.createPost(createDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Post created successfully", created));
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityPostDto>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody CommunityPostUpdateDto updateDto,
            @AuthenticationPrincipal User currentUser) {
        CommunityPostDto updated = communityService.updatePost(id, updateDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", updated));
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        communityService.deletePost(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityCommentDto>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody CommunityCommentCreateDto createDto,
            @AuthenticationPrincipal User currentUser) {
        CommunityCommentDto comment = communityService.addComment(id, createDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Comment added successfully", comment));
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        communityService.deleteComment(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommunityReportDto>> submitReport(
            @Valid @RequestBody CommunityReportCreateDto createDto,
            @AuthenticationPrincipal User currentUser) {
        CommunityReportDto report = moderationService.submitReport(createDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Report submitted successfully", report));
    }
}
