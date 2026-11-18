package com.marabagam.notification.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.notification.dto.NotificationDto;
import com.marabagam.notification.dto.NotificationPreferenceDto;
import com.marabagam.notification.service.NotificationService;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getUserNotifications(currentUser, pageable);
        long unreadCount = notificationService.getUnreadCount(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadCount(@AuthenticationPrincipal User currentUser) {
        long unreadCount = notificationService.getUnreadCount(currentUser);
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(ApiResponse.success("Unread count retrieved", response));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        NotificationDto notification = notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> getPreferences(@AuthenticationPrincipal User currentUser) {
        NotificationPreferenceDto preferences = notificationService.getPreferences(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Preferences retrieved", preferences));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferenceDto>> updatePreferences(
            @Valid @RequestBody NotificationPreferenceDto preferenceDto,
            @AuthenticationPrincipal User currentUser) {
        NotificationPreferenceDto updated = notificationService.updatePreferences(currentUser, preferenceDto);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", updated));
    }
}
