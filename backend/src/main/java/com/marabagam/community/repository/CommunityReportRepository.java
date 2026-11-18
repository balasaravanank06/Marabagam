package com.marabagam.community.repository;

import com.marabagam.community.domain.CommunityReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityReportRepository extends JpaRepository<CommunityReport, UUID> {

    @Query("SELECT r FROM CommunityReport r WHERE r.reporter.id = :reporterId AND r.targetType = :targetType AND r.targetId = :targetId")
    Optional<CommunityReport> findByReporterIdAndTargetTypeAndTargetId(@Param("reporterId") UUID reporterId, @Param("targetType") String targetType, @Param("targetId") UUID targetId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM CommunityReport r WHERE r.reporter.id = :reporterId AND r.targetType = :targetType AND r.targetId = :targetId")
    boolean existsByReporterIdAndTargetTypeAndTargetId(@Param("reporterId") UUID reporterId, @Param("targetType") String targetType, @Param("targetId") UUID targetId);

    Page<CommunityReport> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM CommunityReport r WHERE r.reporter.id = :reporterId AND r.createdAt > :after")
    long countByReporterIdAndCreatedAtAfter(@Param("reporterId") UUID reporterId, @Param("after") Instant after);
}
