package com.marabagam.art.dto;

import java.time.Instant;
import java.util.UUID;

public class EventRegistrationDto {

    private UUID id;
    private UUID eventId;
    private String eventTitle;
    private String eventTitleTa;
    private UUID userId;
    private String userName;
    private String status;
    private Instant registeredAt;
    private Instant cancelledAt;

    public EventRegistrationDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventTitleTa() {
        return eventTitleTa;
    }

    public void setEventTitleTa(String eventTitleTa) {
        this.eventTitleTa = eventTitleTa;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
