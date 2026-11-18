package com.marabagam.notification.dto;

public class NotificationPreferenceDto {

    private Boolean eventsEnabled = true;
    private Boolean communityEnabled = true;
    private Boolean achievementsEnabled = true;

    public NotificationPreferenceDto() {
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
}
