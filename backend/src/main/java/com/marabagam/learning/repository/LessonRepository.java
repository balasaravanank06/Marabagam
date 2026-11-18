package com.marabagam.learning.repository;

import com.marabagam.learning.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByDisplayOrderAsc(UUID moduleId);

    boolean existsByModuleIdAndDisplayOrder(UUID moduleId, Integer displayOrder);

    @Query("SELECT l FROM Lesson l WHERE l.id = :lessonId " +
           "AND l.module.course.slug = :courseSlug " +
           "AND l.status = com.marabagam.learning.domain.LessonStatus.PUBLISHED " +
           "AND l.module.course.status = com.marabagam.learning.domain.CourseStatus.PUBLISHED " +
           "AND l.module.course.artForm.status = com.marabagam.art.domain.ArtFormStatus.PUBLISHED")
    Optional<Lesson> findPublishedLessonByCourseSlugAndId(
            @Param("courseSlug") String courseSlug,
            @Param("lessonId") UUID lessonId
    );

    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.module.course.id = :courseId AND l.status = com.marabagam.learning.domain.LessonStatus.PUBLISHED")
    long countPublishedLessonsByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT l FROM Lesson l WHERE l.module.course.id = :courseId AND l.status = com.marabagam.learning.domain.LessonStatus.PUBLISHED ORDER BY l.module.displayOrder ASC, l.displayOrder ASC")
    List<Lesson> findPublishedLessonsByCourseId(@Param("courseId") UUID courseId);
}
