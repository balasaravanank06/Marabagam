package com.marabagam.art.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class EventCreateDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Tamil title is required")
    private String titleTa;

    @NotNull(message = "District ID is required")
    private Long districtId;

    private UUID artFormId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Tamil description is required")
    private String descriptionTa;

    private String eventType = "RECURRING_PERIOD";
    private String eventPeriod = "Special Event";
    private String eventPeriodTa = "சிறப்பு நிகழ்வு";

    private Boolean isDateApproximate = false;

    private String venueName;
    private String venueNameTa;
    private Double latitude;
    private Double longitude;
    private String provenanceSource;

    @Min(value = 1, message = "Capacity must be greater than zero if specified")
    private Integer capacity;

    private Instant registrationDeadline;
    private Boolean registrationRequired = false;
    private Boolean isOnline = false;
    private String meetingUrl;
    private String organizerName;
    private UUID instructorId;

    private Instant startAt;
    private Instant endAt;
    private String timezoneIdentifier = "Asia/Kolkata";

    public EventCreateDto() {
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

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public UUID getArtFormId() {
        return artFormId;
    }

    public void setArtFormId(UUID artFormId) {
        this.artFormId = artFormId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionTa() {
        return descriptionTa;
    }

    public void setDescriptionTa(String descriptionTa) {
        this.descriptionTa = descriptionTa;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventPeriod() {
        return eventPeriod;
    }

    public void setEventPeriod(String eventPeriod) {
        this.eventPeriod = eventPeriod;
    }

    public String getEventPeriodTa() {
        return eventPeriodTa;
    }

    public void setEventPeriodTa(String eventPeriodTa) {
        this.eventPeriodTa = eventPeriodTa;
    }

    public Boolean getIsDateApproximate() {
        return isDateApproximate;
    }

    public void setIsDateApproximate(Boolean isDateApproximate) {
        this.isDateApproximate = isDateApproximate;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueNameTa() {
        return venueNameTa;
    }

    public void setVenueNameTa(String venueNameTa) {
        this.venueNameTa = venueNameTa;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getProvenanceSource() {
        return provenanceSource;
    }

    public void setProvenanceSource(String provenanceSource) {
        this.provenanceSource = provenanceSource;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Instant getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(Instant registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public Boolean getRegistrationRequired() {
        return registrationRequired;
    }

    public void setRegistrationRequired(Boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(UUID instructorId) {
        this.instructorId = instructorId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public String getTimezoneIdentifier() {
        return timezoneIdentifier;
    }

    public void setTimezoneIdentifier(String timezoneIdentifier) {
        this.timezoneIdentifier = timezoneIdentifier;
    }
}
