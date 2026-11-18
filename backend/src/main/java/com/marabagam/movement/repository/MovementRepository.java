package com.marabagam.movement.repository;

import com.marabagam.movement.domain.Movement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovementRepository extends JpaRepository<Movement, UUID> {
    List<Movement> findByIsActiveTrue();
    List<Movement> findByArtFormIdAndIsActiveTrue(UUID artFormId);
    Optional<Movement> findByCode(String code);
}
