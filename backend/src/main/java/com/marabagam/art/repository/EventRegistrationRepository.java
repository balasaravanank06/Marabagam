package com.marabagam.art.repository;

import com.marabagam.art.domain.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.user.id = :userId AND r.status = :status")
    Optional<EventRegistration> findByEventIdAndUserIdAndStatus(@Param("eventId") UUID eventId, @Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = :status")
    long countByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") String status);

    @Query("SELECT r FROM EventRegistration r WHERE r.user.id = :userId")
    List<EventRegistration> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT r FROM EventRegistration r WHERE r.user.id = :userId ORDER BY r.registeredAt DESC")
    List<EventRegistration> findByUserIdOrderByRegisteredAtDesc(@Param("userId") UUID userId);

    @Query("SELECT r FROM EventRegistration r WHERE r.user.id = :userId AND r.status = :status")
    List<EventRegistration> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId AND r.status = :status")
    List<EventRegistration> findByEventIdAndStatus(@Param("eventId") UUID eventId, @Param("status") String status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM EventRegistration r WHERE r.event.id = :eventId AND r.user.id = :userId AND r.status = :status")
    boolean existsByEventIdAndUserIdAndStatus(@Param("eventId") UUID eventId, @Param("userId") UUID userId, @Param("status") String status);
}
