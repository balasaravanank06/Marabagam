package com.marabagam.movement.repository;

import com.marabagam.movement.domain.PracticeFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PracticeFeedbackRepository extends JpaRepository<PracticeFeedback, UUID> {
    List<PracticeFeedback> findByPracticeSessionIdOrderByCreatedAtAsc(UUID practiceSessionId);
}
