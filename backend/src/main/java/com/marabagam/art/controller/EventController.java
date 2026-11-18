package com.marabagam.art.controller;

import com.marabagam.art.dto.CulturalEventDto;
import com.marabagam.art.dto.EventCreateDto;
import com.marabagam.art.dto.EventRegistrationDto;
import com.marabagam.art.dto.EventUpdateDto;
import com.marabagam.art.service.EventRegistrationService;
import com.marabagam.art.service.EventService;
import com.marabagam.common.response.ApiResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;
    private final EventRegistrationService registrationService;

    public EventController(EventService eventService,
                           EventRegistrationService registrationService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CulturalEventDto>>> getPublicEvents(
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) UUID artFormId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CulturalEventDto> events = eventService.getPublicEvents(districtId, artFormId, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Public events retrieved successfully", events));
    }

    @GetMapping("/my-registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EventRegistrationDto>>> getMyRegistrations(@AuthenticationPrincipal User currentUser) {
        List<EventRegistrationDto> registrations = registrationService.getUserRegistrations(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User registrations retrieved successfully", registrations));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CulturalEventDto>> getEventBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto event = eventService.getEventBySlug(slug, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event details retrieved successfully", event));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> createEvent(
            @Valid @RequestBody EventCreateDto createDto,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto created = eventService.createEvent(createDto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Event created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody EventUpdateDto updateDto,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto updated = eventService.updateEvent(id, updateDto, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", updated));
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> submitEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto submitted = eventService.submitEvent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event submitted for review", submitted));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> approveEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto approved = eventService.approveEvent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event approved", approved));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> publishEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto published = eventService.publishEvent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event published", published));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<CulturalEventDto>> cancelEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        CulturalEventDto cancelled = eventService.cancelEvent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", cancelled));
    }

    @PostMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> registerForEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        EventRegistrationDto reg = registrationService.registerForEvent(id, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registered for event successfully", reg));
    }

    @PostMapping("/{id}/cancel-registration")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> cancelRegistrationPost(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        EventRegistrationDto reg = registrationService.cancelRegistration(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled successfully", reg));
    }

    @DeleteMapping("/{id}/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventRegistrationDto>> cancelRegistrationDelete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        EventRegistrationDto reg = registrationService.cancelRegistration(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Registration cancelled successfully", reg));
    }
}
