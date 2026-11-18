package com.marabagam.quiz.repository;

import com.marabagam.quiz.domain.QuizAttempt;
import com.marabagam.quiz.domain.AttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findByQuizIdAndUserIdOrderByAttemptNumberDesc(UUID quizId, UUID userId);
    Optional<QuizAttempt> findFirstByQuizIdAndUserIdAndStatus(UUID quizId, UUID userId, AttemptStatus status);
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(UUID userId);
    int countByQuizIdAndUserIdAndStatusNot(UUID quizId, UUID userId, AttemptStatus status);
}
