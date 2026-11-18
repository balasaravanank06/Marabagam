package com.marabagam.learning.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.learning.dto.CourseProgressDto;
import com.marabagam.learning.dto.LessonProgressDto;
import com.marabagam.learning.dto.UpdateLessonProgressRequestDto;
import com.marabagam.learning.service.LessonProgressService;
import com.marabagam.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LessonProgressController {

    @Autowired
    private LessonProgressService lessonProgressService;

    @PutMapping("/lessons/{lessonId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LessonProgressDto>> updateProgress(
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateLessonProgressRequestDto request,
            @AuthenticationPrincipal User currentUser
    ) {
        LessonProgressDto progress = lessonProgressService.updateLessonProgress(lessonId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lesson progress updated successfully", progress));
    }

    @GetMapping("/courses/{courseId}/progress")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CourseProgressDto>> getCourseProgress(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        CourseProgressDto courseProgress = lessonProgressService.getCourseProgress(courseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Course progress fetched successfully", courseProgress));
    }
}
