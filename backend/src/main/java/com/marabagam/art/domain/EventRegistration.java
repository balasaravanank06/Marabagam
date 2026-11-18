package com.marabagam.art.domain;

import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_registrations")
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private CulturalEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String status = "REGISTERED"; // 'REGISTERED', 'CANCELLED'

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    public EventRegistration() {
    }

    @PrePersist
    protected void onCreate() {
        this.registeredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CulturalEvent getEvent() {
        return event;
    }

    public UUID getEventId() {
        return event != null ? event.getId() : null;
    }

    public void setEvent(CulturalEvent event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUser(User user) {
        this.user = user;
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
