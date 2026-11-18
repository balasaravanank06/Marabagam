package com.marabagam.art.repository;

import com.marabagam.art.domain.CulturalEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CulturalEventRepository extends JpaRepository<CulturalEvent, UUID> {
    Optional<CulturalEvent> findBySlug(String slug);
    List<CulturalEvent> findByDistrictIdAndStatus(Long districtId, String status);
    List<CulturalEvent> findByArtFormSlugAndStatus(String slug, String status);
    List<CulturalEvent> findByStatus(String status);
    @Query("SELECT e FROM CulturalEvent e WHERE e.createdBy.id = :createdById")
    List<CulturalEvent> findByCreatedById(@Param("createdById") UUID createdById);
    long countByDistrictIdAndStatus(Long districtId, String status);
    long countByStatus(String status);

    Page<CulturalEvent> findByStatus(String status, Pageable pageable);
    Page<CulturalEvent> findByDistrictIdAndStatus(Long districtId, String status, Pageable pageable);
    Page<CulturalEvent> findByArtFormIdAndStatus(UUID artFormId, String status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM CulturalEvent e WHERE e.id = :id")
    Optional<CulturalEvent> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT e FROM CulturalEvent e WHERE e.status = :status AND e.startAt >= :startWindow AND e.startAt <= :endWindow")
    List<CulturalEvent> findUpcomingEventsInWindow(@Param("status") String status,
                                                    @Param("startWindow") Instant startWindow,
                                                    @Param("endWindow") Instant endWindow);
}
