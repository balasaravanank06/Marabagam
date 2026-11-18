package com.marabagam.learning.service;

import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.common.exception.AuthException;
import com.marabagam.learning.domain.*;
import com.marabagam.learning.dto.CourseProgressDto;
import com.marabagam.learning.dto.LessonProgressDto;
import com.marabagam.learning.dto.UpdateLessonProgressRequestDto;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.learning.repository.EnrollmentRepository;
import com.marabagam.learning.repository.LessonProgressRepository;
import com.marabagam.learning.repository.LessonRepository;
import com.marabagam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LessonProgressService {

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Transactional
    public LessonProgressDto updateLessonProgress(UUID lessonId, UpdateLessonProgressRequestDto request, User currentUser) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Lesson not found with ID: " + lessonId));

        Course course = lesson.getModule().getCourse();
        if (lesson.getStatus() != LessonStatus.PUBLISHED ||
            course.getStatus() != CourseStatus.PUBLISHED ||
            course.getArtForm().getStatus() != ArtFormStatus.PUBLISHED) {
            throw new AuthException("RESOURCE_NOT_FOUND", "Cannot update progress on unpublished lessons");
        }

        // Validate progress bounds 0..100
        int progressPercent = request.getProgressPercent();
        if (progressPercent < 0 || progressPercent > 100) {
            throw new AuthException("INVALID_INPUT", "Progress percent must be between 0 and 100");
        }

        Boolean requestedCompleted = request.getCompleted();

        // Enforce completed consistency: completed=true requires progressPercent=100
        if (Boolean.TRUE.equals(requestedCompleted) && progressPercent < 100) {
            throw new AuthException("INVALID_INPUT", "Cannot set completed=true when progress is less than 100%");
        }

        // Setting progressPercent=100 automatically sets completed=true
        boolean isNowCompleted = (progressPercent == 100) || Boolean.TRUE.equals(requestedCompleted);

        Optional<LessonProgress> existingOpt = lessonProgressRepository.findByUserIdAndLessonId(currentUser.getId(), lessonId);

        LessonProgress progress;
        if (existingOpt.isPresent()) {
            progress = existingOpt.get();
            // Ownership check (IDOR protection)
            if (!progress.getUser().getId().equals(currentUser.getId())) {
                throw new AuthException("FORBIDDEN_ACTION", "Cannot modify another user's lesson progress");
            }
            // Rule: Completed lessons cannot be reverted to incomplete by normal learner endpoints
            if (Boolean.TRUE.equals(progress.getCompleted()) && !isNowCompleted) {
                throw new AuthException("INVALID_INPUT", "Completed lessons cannot be reverted to incomplete status");
            }
        } else {
            progress = new LessonProgress();
            progress.setUser(currentUser);
            progress.setLesson(lesson);
        }

        progress.setProgressPercent(progressPercent);

        if (isNowCompleted) {
            progress.setCompleted(true);
            if (progress.getCompletedAt() == null) {
                progress.setCompletedAt(Instant.now());
            }
        } else {
            progress.setCompleted(false);
        }

        progress.setLastAccessedAt(Instant.now());

        LessonProgress saved = lessonProgressRepository.save(progress);

        // Calculate course completion and update enrollment status if completed
        checkAndUpdateCourseCompletion(currentUser.getId(), course.getId());

        return LessonProgressDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public CourseProgressDto getCourseProgress(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + courseId));

        long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
        long completedPublished = lessonProgressRepository.countCompletedPublishedLessonsByUserIdAndCourseId(currentUser.getId(), courseId);

        int completionPercent = totalPublished > 0 ? (int) Math.round(((double) completedPublished / totalPublished) * 100.0) : 0;
        boolean isCourseCompleted = (totalPublished > 0) && (completedPublished == totalPublished);

        List<LessonProgress> userProgressList = lessonProgressRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);

        UUID lastAccessedLessonId = userProgressList.stream()
                .max((a, b) -> a.getLastAccessedAt().compareTo(b.getLastAccessedAt()))
                .map(lp -> lp.getLesson().getId())
                .orElse(null);

        // Find current uncompleted lesson
        List<Lesson> publishedLessons = lessonRepository.findPublishedLessonsByCourseId(courseId);
        UUID currentLessonId = publishedLessons.stream()
                .filter(l -> userProgressList.stream().noneMatch(lp -> lp.getLesson().getId().equals(l.getId()) && Boolean.TRUE.equals(lp.getCompleted())))
                .findFirst()
                .map(Lesson::getId)
                .orElse(publishedLessons.isEmpty() ? null : publishedLessons.get(publishedLessons.size() - 1).getId());

        return new CourseProgressDto(
                courseId,
                totalPublished,
                completedPublished,
                completionPercent,
                currentLessonId,
                lastAccessedLessonId,
                isCourseCompleted
        );
    }

    @Transactional
    public void checkAndUpdateCourseCompletion(UUID userId, UUID courseId) {
        long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
        long completedPublished = lessonProgressRepository.countCompletedPublishedLessonsByUserIdAndCourseId(userId, courseId);

        if (totalPublished > 0 && completedPublished == totalPublished) {
            enrollmentRepository.findByUserIdAndCourseId(userId, courseId).ifPresent(enrollment -> {
                if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
                    enrollment.setStatus(EnrollmentStatus.COMPLETED);
                    enrollment.setCompletedAt(Instant.now());
                    enrollmentRepository.save(enrollment);
                }
            });
        }
    }
}
