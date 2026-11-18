package com.marabagam.community.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.community.dto.CommunityReportDto;
import com.marabagam.community.service.ModerationService;
import com.marabagam.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/community")
public class AdminCommunityController {

    private final ModerationService moderationService;

    public AdminCommunityController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<CommunityReportDto>>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityReportDto> reports = moderationService.getPendingReports(pageable);
        return ResponseEntity.ok(ApiResponse.success("Pending reports retrieved", reports));
    }

    @PutMapping("/reports/{id}/action")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CommunityReportDto>> actionReport(
            @PathVariable UUID id,
            @RequestParam String action, // 'DISMISS', 'HIDE', 'REMOVE'
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal User currentUser) {
        CommunityReportDto report = moderationService.actionReport(id, action, notes, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Report actioned", report));
    }
}
