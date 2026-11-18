package com.marabagam.learning.repository;

import com.marabagam.learning.domain.Enrollment;
import com.marabagam.learning.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    Optional<Enrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);
    Optional<Enrollment> findByUserIdAndCourseIdAndStatus(UUID userId, UUID courseId, EnrollmentStatus status);
    List<Enrollment> findByUserId(UUID userId);
    List<Enrollment> findByUserIdAndStatus(UUID userId, EnrollmentStatus status);
    boolean existsByUserIdAndCourseIdAndStatus(UUID userId, UUID courseId, EnrollmentStatus status);
}
