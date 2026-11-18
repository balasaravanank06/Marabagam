package com.marabagam.notification.repository;

import com.marabagam.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("recipientId") UUID recipientId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(@Param("recipientId") UUID recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false")
    long countByRecipientIdAndIsReadFalse(@Param("recipientId") UUID recipientId);

    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n WHERE n.recipient.id = :recipientId AND n.deduplicationKey = :deduplicationKey")
    boolean existsByRecipientIdAndDeduplicationKey(@Param("recipientId") UUID recipientId, @Param("deduplicationKey") String deduplicationKey);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.deduplicationKey = :deduplicationKey")
    Optional<Notification> findByRecipientIdAndDeduplicationKey(@Param("recipientId") UUID recipientId, @Param("deduplicationKey") String deduplicationKey);
}
