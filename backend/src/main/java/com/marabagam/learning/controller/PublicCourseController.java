package com.marabagam.learning.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.learning.domain.CourseDifficulty;
import com.marabagam.learning.dto.CourseDetailDto;
import com.marabagam.learning.dto.CourseSummaryDto;
import com.marabagam.learning.dto.LessonDetailDto;
import com.marabagam.learning.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
public class PublicCourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseSummaryDto>>> getPublicCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String artForm,
            @RequestParam(required = false) CourseDifficulty difficulty
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseSummaryDto> courses = courseService.getPublicCourses(search, artForm, difficulty, pageable);
        return ResponseEntity.ok(ApiResponse.success("Public courses fetched successfully", courses));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CourseDetailDto>> getCourseBySlug(@PathVariable String slug) {
        CourseDetailDto course = courseService.getPublishedCourseBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Course details fetched successfully", course));
    }

    @GetMapping("/{slug}/lessons/{lessonId}")
    public ResponseEntity<ApiResponse<LessonDetailDto>> getLessonDetail(
            @PathVariable String slug,
            @PathVariable UUID lessonId
    ) {
        LessonDetailDto lesson = courseService.getPublishedLesson(slug, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Published lesson fetched successfully", lesson));
    }
}
