package com.marabagam.learning.repository;

import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseDifficulty;
import com.marabagam.learning.domain.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    Optional<Course> findBySlug(String slug);

    @Query("SELECT c FROM Course c WHERE c.slug = :slug AND c.status = com.marabagam.learning.domain.CourseStatus.PUBLISHED AND c.artForm.status = com.marabagam.art.domain.ArtFormStatus.PUBLISHED")
    Optional<Course> findPublishedBySlug(@Param("slug") String slug);

    @Query("SELECT c FROM Course c WHERE c.status = com.marabagam.learning.domain.CourseStatus.PUBLISHED AND c.artForm.status = com.marabagam.art.domain.ArtFormStatus.PUBLISHED " +
           "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(c.titleTa) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(c.shortDescription) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (:artFormSlug IS NULL OR LOWER(c.artForm.slug) = LOWER(CAST(:artFormSlug AS string))) " +
           "AND (:difficulty IS NULL OR c.difficulty = :difficulty)")
    Page<Course> findPublicCourses(
            @Param("search") String search,
            @Param("artFormSlug") String artFormSlug,
            @Param("difficulty") CourseDifficulty difficulty,
            Pageable pageable
    );

    @Query("SELECT c FROM Course c WHERE " +
           "(:status IS NULL OR c.status = :status) " +
           "AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(c.titleTa) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(c.slug) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<Course> findAdminCourses(
            @Param("status") CourseStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    java.util.List<Course> findByStatus(CourseStatus status);

    default java.util.List<Course> findByPublishedTrue() {
        return findByStatus(CourseStatus.PUBLISHED);
    }
}

