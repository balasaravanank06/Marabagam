package com.marabagam.quiz.repository;

import com.marabagam.quiz.domain.QuizAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswer, UUID> {
    List<QuizAttemptAnswer> findByAttemptId(UUID attemptId);
}
