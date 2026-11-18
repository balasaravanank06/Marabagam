package com.marabagam.quiz.repository;

import com.marabagam.quiz.domain.Quiz;
import com.marabagam.quiz.domain.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByCourseIdAndStatus(UUID courseId, QuizStatus status);
    List<Quiz> findByModuleIdAndStatus(UUID moduleId, QuizStatus status);
    List<Quiz> findByLessonIdAndStatus(UUID lessonId, QuizStatus status);
    List<Quiz> findByStatus(QuizStatus status);
    Optional<Quiz> findByIdAndStatus(UUID id, QuizStatus status);
    List<Quiz> findByCreatedById(UUID createdById);
}
