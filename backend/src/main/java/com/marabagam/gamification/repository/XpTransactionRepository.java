package com.marabagam.gamification.repository;

import com.marabagam.gamification.domain.XpTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface XpTransactionRepository extends JpaRepository<XpTransaction, UUID> {
    Optional<XpTransaction> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM XpTransaction t WHERE t.user.id = :userId")
    int sumAmountByUserId(@Param("userId") UUID userId);

    @Query("SELECT t.user.id AS userId, t.user.displayName AS displayName, t.user.leaderboardVisible AS leaderboardVisible, SUM(t.amount) AS totalXp " +
           "FROM XpTransaction t " +
           "WHERE t.user.leaderboardVisible = true " +
           "GROUP BY t.user.id, t.user.displayName, t.user.leaderboardVisible " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> findGlobalLeaderboard(Pageable pageable);

    @Query("SELECT t.user.id AS userId, t.user.displayName AS displayName, t.user.leaderboardVisible AS leaderboardVisible, SUM(t.amount) AS weeklyXp " +
           "FROM XpTransaction t " +
           "WHERE t.user.leaderboardVisible = true AND t.createdAt >= :startDate AND t.createdAt < :endDate " +
           "GROUP BY t.user.id, t.user.displayName, t.user.leaderboardVisible " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> findWeeklyLeaderboard(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);
}
