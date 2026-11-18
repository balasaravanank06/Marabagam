package com.marabagam.learning.service;

import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.common.exception.AuthException;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseStatus;
import com.marabagam.learning.domain.Enrollment;
import com.marabagam.learning.domain.EnrollmentStatus;
import com.marabagam.learning.dto.EnrollmentDto;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.learning.repository.EnrollmentRepository;
import com.marabagam.learning.repository.LessonProgressRepository;
import com.marabagam.learning.repository.LessonRepository;
import com.marabagam.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Transactional
    public EnrollmentDto enrollCourse(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Course not found with ID: " + courseId));

        if (course.getStatus() != CourseStatus.PUBLISHED || course.getArtForm().getStatus() != ArtFormStatus.PUBLISHED) {
            throw new AuthException("RESOURCE_NOT_FOUND", "Cannot enroll in unpublished courses");
        }

        Optional<Enrollment> existingOpt = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId);

        Enrollment enrollment;
        if (existingOpt.isPresent()) {
            enrollment = existingOpt.get();
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE || enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
                // Return existing enrollment
                return enrichEnrollmentDto(EnrollmentDto.fromEntity(enrollment), currentUser.getId(), courseId);
            }
            // Reactivate cancelled enrollment
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setCompletedAt(null);
        } else {
            enrollment = new Enrollment();
            enrollment.setUser(currentUser);
            enrollment.setCourse(course);
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        return enrichEnrollmentDto(EnrollmentDto.fromEntity(saved), currentUser.getId(), courseId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> getUserEnrollments(User currentUser) {
        return enrollmentRepository.findByUserId(currentUser.getId()).stream()
                .map(e -> enrichEnrollmentDto(EnrollmentDto.fromEntity(e), currentUser.getId(), e.getCourse().getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentDto getUserEnrollmentForCourse(UUID courseId, User currentUser) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Enrollment not found for this course"));
        return enrichEnrollmentDto(EnrollmentDto.fromEntity(enrollment), currentUser.getId(), courseId);
    }

    @Transactional
    public EnrollmentDto cancelEnrollment(UUID courseId, User currentUser) {
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> new AuthException("RESOURCE_NOT_FOUND", "Active enrollment not found for this course"));

        // Mandatory Soft Cancellation rule: ACTIVE -> CANCELLED
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        Enrollment saved = enrollmentRepository.save(enrollment);
        return enrichEnrollmentDto(EnrollmentDto.fromEntity(saved), currentUser.getId(), courseId);
    }

    private EnrollmentDto enrichEnrollmentDto(EnrollmentDto dto, UUID userId, UUID courseId) {
        long totalPublished = lessonRepository.countPublishedLessonsByCourseId(courseId);
        long completedPublished = lessonProgressRepository.countCompletedPublishedLessonsByUserIdAndCourseId(userId, courseId);
        int percentage = totalPublished > 0 ? (int) Math.round(((double) completedPublished / totalPublished) * 100.0) : 0;
        dto.setCompletionPercentage(percentage);
        return dto;
    }
}
