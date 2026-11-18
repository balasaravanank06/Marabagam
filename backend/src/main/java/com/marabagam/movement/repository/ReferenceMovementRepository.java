package com.marabagam.movement.repository;

import com.marabagam.movement.domain.ReferenceMovement;
import com.marabagam.movement.domain.ReferenceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferenceMovementRepository extends JpaRepository<ReferenceMovement, UUID> {
    List<ReferenceMovement> findByMovementIdAndStatus(UUID movementId, ReferenceStatus status);
    Optional<ReferenceMovement> findFirstByMovementIdAndStatusOrderByApprovedAtDesc(UUID movementId, ReferenceStatus status);
}
