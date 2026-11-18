package com.marabagam.learning.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.common.exception.AuthException;
import com.marabagam.learning.domain.*;
import com.marabagam.learning.dto.*;
import com.marabagam.learning.repository.*;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    // PUBLIC COURSE & LESSON APIS
    // ============================================================

    @Transactional(readOnly = true)
    public Page<CourseSummaryDto> getPublicCourses(String search, String artFormSlug, CourseDifficulty difficulty, Pageable pageable) {
        return courseRepository.findPublicCourses(search, artFormSlug, difficulty, pageable)
                .map(CourseSummaryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public CourseDetailDto getPublishedCourseBySlug(String slug) {
        Course course = courseRepository.findPublishedBySlug(slug)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Published course not found with slug: " + slug));
        return CourseDetailDto.fromEntity(course, true);
    }

    @Transactional(readOnly = true)
    public LessonDetailDto getPublishedLesson(String courseSlug, UUID lessonId) {
        Lesson lesson = lessonRepository.findPublishedLessonByCourseSlugAndId(courseSlug, lessonId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Published lesson not found or access denied"));

        // Mandatory service-level validation
        Course course = lesson.getModule().getCourse();
        ArtForm artForm = course.getArtForm();

        if (artForm.getStatus() != ArtFormStatus.PUBLISHED ||
            course.getStatus() != CourseStatus.PUBLISHED ||
            lesson.getStatus() != LessonStatus.PUBLISHED ||
            !course.getSlug().equalsIgnoreCase(courseSlug)) {
            throw new AuthException("RESOURCE_NOT_FOUND", "Lesson access isolated");
        }

        return LessonDetailDto.fromEntity(lesson);
    }

    // ============================================================
    // ADMIN / CONTENT AUTHORING & GOVERNANCE APIS
    // ============================================================

    @Transactional(readOnly = true)
    public Page<CourseSummaryDto> getAdminCourses(CourseStatus status, String search, Pageable pageable, User currentUser) {
        // ARTIST / INSTRUCTOR can view courses
        return courseRepository.findAdminCourses(status, search, pageable)
                .map(CourseSummaryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public CourseDetailDto getAdminCourseById(UUID id, User currentUser) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + id));
        return CourseDetailDto.fromEntity(course, false);
    }

    @Transactional
    public CourseDetailDto createCourse(CreateCourseRequestDto request, User currentUser) {
        ArtForm artForm = artFormRepository.findById(request.getArtFormId())
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Art form not found with ID: " + request.getArtFormId()));

        if (courseRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new AuthException("DUPLICATE_RESOURCE", "A course with slug '" + request.getSlug() + "' already exists");
        }

        Course course = new Course();
        course.setArtForm(artForm);
        course.setTitle(request.getTitle());
        course.setTitleTa(request.getTitleTa());
        course.setSlug(request.getSlug());
        course.setShortDescription(request.getShortDescription());
        course.setShortDescriptionTa(request.getShortDescriptionTa());
        course.setDescription(request.getDescription());
        course.setDescriptionTa(request.getDescriptionTa());
        course.setDifficulty(request.getDifficulty());
        course.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes() != null ? request.getEstimatedDurationMinutes() : 0);
        course.setStatus(CourseStatus.DRAFT);
        course.setCreatedBy(currentUser);

        Course saved = courseRepository.save(course);
        return CourseDetailDto.fromEntity(saved, false);
    }

    @Transactional
    public CourseDetailDto updateCourse(UUID courseId, UpdateCourseRequestDto request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + courseId));

        // Author ownership check for ARTIST / INSTRUCTOR
        verifyAuthorEditPermission(course, currentUser);

        if (!course.getSlug().equalsIgnoreCase(request.getSlug()) && courseRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new AuthException("DUPLICATE_RESOURCE", "A course with slug '" + request.getSlug() + "' already exists");
        }

        course.setTitle(request.getTitle());
        course.setTitleTa(request.getTitleTa());
        course.setSlug(request.getSlug());
        course.setShortDescription(request.getShortDescription());
        course.setShortDescriptionTa(request.getShortDescriptionTa());
        course.setDescription(request.getDescription());
        course.setDescriptionTa(request.getDescriptionTa());
        course.setDifficulty(request.getDifficulty());
        if (request.getEstimatedDurationMinutes() != null) {
            course.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        }

        Course saved = courseRepository.save(course);
        return CourseDetailDto.fromEntity(saved, false);
    }

    @Transactional
    public CourseDetailDto updateCourseStatus(UUID courseId, CourseStatus targetStatus, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + courseId));

        Role role = currentUser.getRole();

        if (role == Role.ARTIST || role == Role.INSTRUCTOR) {
            // Can only submit their own draft for review
            if (!course.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new AuthException("FORBIDDEN_ACTION", "Contributors can only modify their own courses");
            }
            if (targetStatus == CourseStatus.PENDING_REVIEW && (course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.PENDING_REVIEW)) {
                course.setStatus(CourseStatus.PENDING_REVIEW);
            } else if (targetStatus == CourseStatus.DRAFT) {
                course.setStatus(CourseStatus.DRAFT);
            } else {
                throw new AuthException("FORBIDDEN_ACTION", "Contributors cannot directly approve, publish, or archive courses");
            }
        } else if (role == Role.CULTURAL_EXPERT) {
            if (targetStatus == CourseStatus.APPROVED || targetStatus == CourseStatus.DRAFT || targetStatus == CourseStatus.PENDING_REVIEW) {
                course.setStatus(targetStatus);
            } else {
                throw new AuthException("FORBIDDEN_ACTION", "Cultural Experts can approve content but cannot publish or archive");
            }
        } else if (role == Role.ADMIN) {
            course.setStatus(targetStatus);
        } else {
            throw new AuthException("FORBIDDEN_ACTION", "User role does not have status management privileges");
        }

        Course saved = courseRepository.save(course);
        return CourseDetailDto.fromEntity(saved, false);
    }

    // ============================================================
    // MODULE MANAGEMENT
    // ============================================================

    @Transactional
    public CourseModuleDto addModule(UUID courseId, CreateModuleRequestDto request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + courseId));

        verifyAuthorEditPermission(course, currentUser);

        if (moduleRepository.existsByCourseIdAndDisplayOrder(courseId, request.getDisplayOrder())) {
            throw new AuthException("DUPLICATE_RESOURCE", "A module with display order " + request.getDisplayOrder() + " already exists in this course");
        }

        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setTitle(request.getTitle());
        module.setTitleTa(request.getTitleTa());
        module.setDescription(request.getDescription());
        module.setDescriptionTa(request.getDescriptionTa());
        module.setDisplayOrder(request.getDisplayOrder());

        CourseModule saved = moduleRepository.save(module);
        return CourseModuleDto.fromEntity(saved, false);
    }

    // ============================================================
    // LESSON MANAGEMENT
    // ============================================================

    @Transactional
    public LessonDetailDto addLesson(UUID moduleId, CreateLessonRequestDto request, User currentUser) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Module not found with ID: " + moduleId));

        verifyAuthorEditPermission(module.getCourse(), currentUser);

        if (lessonRepository.existsByModuleIdAndDisplayOrder(moduleId, request.getDisplayOrder())) {
            throw new AuthException("DUPLICATE_RESOURCE", "A lesson with display order " + request.getDisplayOrder() + " already exists in this module");
        }

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.getTitle());
        lesson.setTitleTa(request.getTitleTa());
        lesson.setDescription(request.getDescription());
        lesson.setDescriptionTa(request.getDescriptionTa());
        lesson.setContent(request.getContent());
        lesson.setContentTa(request.getContentTa());
        lesson.setLessonType(request.getLessonType());
        lesson.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes() != null ? request.getEstimatedDurationMinutes() : 0);
        lesson.setDisplayOrder(request.getDisplayOrder());
        lesson.setCreatedBy(currentUser);

        Role role = currentUser.getRole();
        if (role == Role.ARTIST || role == Role.INSTRUCTOR) {
            lesson.setStatus(LessonStatus.DRAFT);
        } else {
            lesson.setStatus(request.getStatus() != null ? request.getStatus() : LessonStatus.DRAFT);
        }

        Lesson savedLesson = lessonRepository.save(lesson);

        // Process Media Items with URL Security Validation
        if (request.getMediaItems() != null && !request.getMediaItems().isEmpty()) {
            for (CreateLessonRequestDto.MediaItemRequestDto mediaReq : request.getMediaItems()) {
                validateMediaUrl(mediaReq.getUrl());
                if (mediaReq.getThumbnailUrl() != null) {
                    validateMediaUrl(mediaReq.getThumbnailUrl());
                }

                Media media = new Media();
                media.setLesson(savedLesson);
                media.setMediaType(mediaReq.getMediaType());
                media.setTitle(mediaReq.getTitle());
                media.setTitleTa(mediaReq.getTitleTa());
                media.setUrl(mediaReq.getUrl());
                media.setThumbnailUrl(mediaReq.getThumbnailUrl());
                media.setMimeType(mediaReq.getMimeType());
                media.setFileSizeBytes(mediaReq.getFileSizeBytes());
                media.setDurationSeconds(mediaReq.getDurationSeconds());
                media.setDisplayOrder(mediaReq.getDisplayOrder() != null ? mediaReq.getDisplayOrder() : 1);
                media.setAltText(mediaReq.getAltText());
                media.setAltTextTa(mediaReq.getAltTextTa());
                mediaRepository.save(media);
            }
        }

        return LessonDetailDto.fromEntity(lessonRepository.findById(savedLesson.getId()).orElse(savedLesson));
    }

    // ============================================================
    // HELPER SECURITY VALIDATIONS
    // ============================================================

    private void verifyAuthorEditPermission(Course course, User currentUser) {
        Role role = currentUser.getRole();
        if (role == Role.ARTIST || role == Role.INSTRUCTOR) {
            if (!course.getCreatedBy().getId().equals(currentUser.getId())) {
                throw new AuthException("FORBIDDEN_ACTION", "Contributors can only edit their own courses");
            }
            if (course.getStatus() == CourseStatus.PUBLISHED || course.getStatus() == CourseStatus.ARCHIVED) {
                throw new AuthException("FORBIDDEN_ACTION", "Cannot modify courses that are already published or archived");
            }
        }
    }

    private void validateMediaUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            throw new AuthException("INVALID_INPUT", "Media URL cannot be empty");
        }
        if (urlString.length() > 2048) {
            throw new AuthException("INVALID_INPUT", "Media URL exceeds maximum length of 2048 characters");
        }
        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new AuthException("INVALID_INPUT", "Media URL scheme must be HTTP or HTTPS");
            }
        } catch (Exception e) {
            throw new AuthException("INVALID_INPUT", "Invalid media URL syntax: " + e.getMessage());
        }
    }
}
