package com.marabagam.movement.repository;

import com.marabagam.movement.domain.MovementVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovementVersionRepository extends JpaRepository<MovementVersion, UUID> {
    Optional<MovementVersion> findByMovementIdAndIsCurrentTrue(UUID movementId);
    Optional<MovementVersion> findByMovementIdAndVersionNumber(UUID movementId, String versionNumber);
}
