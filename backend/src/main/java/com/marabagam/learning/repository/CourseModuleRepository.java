package com.marabagam.learning.repository;

import com.marabagam.learning.domain.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {
    List<CourseModule> findByCourseIdOrderByDisplayOrderAsc(UUID courseId);
    boolean existsByCourseIdAndDisplayOrder(UUID courseId, Integer displayOrder);
}
