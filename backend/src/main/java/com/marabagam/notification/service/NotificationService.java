package com.marabagam.notification.service;

import com.marabagam.notification.domain.Notification;
import com.marabagam.notification.domain.NotificationPreference;
import com.marabagam.notification.dto.NotificationDto;
import com.marabagam.notification.dto.NotificationPreferenceDto;
import com.marabagam.notification.repository.NotificationPreferenceRepository;
import com.marabagam.notification.repository.NotificationRepository;
import com.marabagam.user.domain.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final com.marabagam.user.repository.UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               com.marabagam.user.repository.UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public NotificationDto sendNotification(UUID recipientId,
                                            String type,
                                            String title,
                                            String titleTa,
                                            String message,
                                            String messageTa,
                                            String targetUrl,
                                            String deduplicationKey) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + recipientId));
        return sendNotification(recipient, type, title, titleTa, message, messageTa, deduplicationKey, null, null);
    }


    @Transactional
    public NotificationDto sendNotification(User recipient,
                                            String type,
                                            String title,
                                            String titleTa,
                                            String message,
                                            String messageTa,
                                            String deduplicationKey,
                                            String relatedType,
                                            UUID relatedId) {
        if (!isNotificationCategoryEnabled(recipient.getId(), type)) {
            return null;
        }

        if (deduplicationKey != null && notificationRepository.existsByRecipientIdAndDeduplicationKey(recipient.getId(), deduplicationKey)) {
            return notificationRepository.findByRecipientIdAndDeduplicationKey(recipient.getId(), deduplicationKey)
                    .map(this::toDto)
                    .orElse(null);
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setTitleTa(titleTa);
        notification.setMessage(message);
        notification.setMessageTa(messageTa);
        notification.setDeduplicationKey(deduplicationKey);
        notification.setRelatedEntityType(relatedType);
        notification.setRelatedEntityId(relatedId);
        notification.setIsRead(false);

        try {
            Notification saved = notificationRepository.save(notification);
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            // Deduplication constraint caught duplicate recipient notification
            return notificationRepository.findByRecipientIdAndDeduplicationKey(recipient.getId(), deduplicationKey)
                    .map(this::toDto)
                    .orElse(null);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public NotificationDto markAsRead(UUID notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized notification access");
        }

        notification.setIsRead(true);
        return toDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceDto getPreferences(User user) {
        NotificationPreference pref = preferenceRepository.findById(user.getId())
                .orElseGet(() -> new NotificationPreference(user));
        return toPreferenceDto(pref);
    }

    @Transactional
    public NotificationPreferenceDto updatePreferences(User user, NotificationPreferenceDto dto) {
        NotificationPreference pref = preferenceRepository.findById(user.getId())
                .orElseGet(() -> new NotificationPreference(user));

        if (dto.getEventsEnabled() != null) pref.setEventsEnabled(dto.getEventsEnabled());
        if (dto.getCommunityEnabled() != null) pref.setCommunityEnabled(dto.getCommunityEnabled());
        if (dto.getAchievementsEnabled() != null) pref.setAchievementsEnabled(dto.getAchievementsEnabled());

        return toPreferenceDto(preferenceRepository.save(pref));
    }

    private boolean isNotificationCategoryEnabled(UUID userId, String type) {
        NotificationPreference pref = preferenceRepository.findById(userId).orElse(null);
        if (pref == null) return true;

        switch (type) {
            case "EVENT_REMINDER":
            case "EVENT_CANCELLED":
                return Boolean.TRUE.equals(pref.getEventsEnabled());
            case "COMMUNITY_REPLY":
                return Boolean.TRUE.equals(pref.getCommunityEnabled());
            case "BADGE_EARNED":
            case "CERTIFICATE_ISSUED":
                return Boolean.TRUE.equals(pref.getAchievementsEnabled());
            case "MODERATION_UPDATE":
            case "ACCOUNT_SECURITY":
            default:
                return true; // Always enabled security & moderation updates
        }
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setNotificationType(n.getNotificationType());
        dto.setTitle(n.getTitle());
        dto.setTitleTa(n.getTitleTa());
        dto.setMessage(n.getMessage());
        dto.setMessageTa(n.getMessageTa());
        dto.setIsRead(n.getIsRead());
        dto.setRelatedEntityType(n.getRelatedEntityType());
        dto.setRelatedEntityId(n.getRelatedEntityId());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    private NotificationPreferenceDto toPreferenceDto(NotificationPreference pref) {
        NotificationPreferenceDto dto = new NotificationPreferenceDto();
        dto.setEventsEnabled(pref.getEventsEnabled());
        dto.setCommunityEnabled(pref.getCommunityEnabled());
        dto.setAchievementsEnabled(pref.getAchievementsEnabled());
        return dto;
    }
}
