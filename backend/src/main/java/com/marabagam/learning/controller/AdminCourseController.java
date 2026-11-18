package com.marabagam.learning.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.learning.domain.CourseStatus;
import com.marabagam.learning.dto.*;
import com.marabagam.learning.service.CourseService;
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
@RequestMapping("/api/v1/admin/courses")
@PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
public class AdminCourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getAdminCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) CourseStatus status,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseSummaryDto> courses = courseService.getAdminCourses(status, search, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Admin courses fetched successfully", courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> getAdminCourseById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseDetailDto course = courseService.getAdminCourseById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Admin course details fetched successfully", course));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDetailDto>> createCourse(
            @Valid @RequestBody CreateCourseRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseDetailDto created = courseService.createCourse(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseDetailDto updated = courseService.updateCourse(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CourseDetailDto>> updateCourseStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseStatusRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseDetailDto updated = courseService.updateCourseStatus(id, request.getStatus(), currentUser);
        return ResponseEntity.ok(ApiResponse.success("Course status updated successfully", updated));
    }

    @PostMapping("/{courseId}/modules")
    public ResponseEntity<ApiResponse<CourseModuleDto>> addModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateModuleRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseModuleDto created = courseService.addModule(courseId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Module added successfully", created));
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<ApiResponse<LessonDetailDto>> addLesson(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateLessonRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        LessonDetailDto created = courseService.addLesson(moduleId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lesson added successfully", created));
    }
}
