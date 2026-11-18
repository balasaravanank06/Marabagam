package com.marabagam.notification.dto;

import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    private UUID id;
    private String notificationType;
    private String title;
    private String titleTa;
    private String message;
    private String messageTa;
    private Boolean isRead;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private Instant createdAt;

    public NotificationDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleTa() {
        return titleTa;
    }

    public void setTitleTa(String titleTa) {
        this.titleTa = titleTa;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageTa() {
        return messageTa;
    }

    public void setMessageTa(String messageTa) {
        this.messageTa = messageTa;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public UUID getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(UUID relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
