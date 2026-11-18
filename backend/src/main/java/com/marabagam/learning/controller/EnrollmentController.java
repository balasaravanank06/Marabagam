package com.marabagam.learning.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.learning.dto.EnrollmentDto;
import com.marabagam.learning.service.EnrollmentService;
import com.marabagam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentDto>> enroll(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        EnrollmentDto enrollment = enrollmentService.enrollCourse(courseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Enrolled in course successfully", enrollment));
    }

    @GetMapping("/users/me/enrollments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EnrollmentDto>>> getMyEnrollments(
            @AuthenticationPrincipal User currentUser
    ) {
        List<EnrollmentDto> enrollments = enrollmentService.getUserEnrollments(currentUser);
        return ResponseEntity.ok(ApiResponse.success("User enrollments fetched successfully", enrollments));
    }

    @GetMapping("/users/me/enrollments/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentDto>> getMyEnrollmentForCourse(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        EnrollmentDto enrollment = enrollmentService.getUserEnrollmentForCourse(courseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("User enrollment fetched successfully", enrollment));
    }

    @DeleteMapping("/users/me/enrollments/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EnrollmentDto>> cancelEnrollment(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser
    ) {
        EnrollmentDto cancelled = enrollmentService.cancelEnrollment(courseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Enrollment cancelled successfully", cancelled));
    }
}
