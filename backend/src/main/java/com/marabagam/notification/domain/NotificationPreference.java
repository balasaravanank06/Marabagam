package com.marabagam.notification.domain;

import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference implements Persistable<UUID> {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "events_enabled", nullable = false)
    private Boolean eventsEnabled = true;

    @Column(name = "community_enabled", nullable = false)
    private Boolean communityEnabled = true;

    @Column(name = "achievements_enabled", nullable = false)
    private Boolean achievementsEnabled = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    public NotificationPreference() {
    }

    public NotificationPreference(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
        this.updatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = Instant.now();
    }

    @PostLoad
    @PostPersist
    protected void markNotNew() {
        this.isNew = false;
    }

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public Boolean getEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(Boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    public Boolean getCommunityEnabled() {
        return communityEnabled;
    }

    public void setCommunityEnabled(Boolean communityEnabled) {
        this.communityEnabled = communityEnabled;
    }

    public Boolean getAchievementsEnabled() {
        return achievementsEnabled;
    }

    public void setAchievementsEnabled(Boolean achievementsEnabled) {
        this.achievementsEnabled = achievementsEnabled;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
