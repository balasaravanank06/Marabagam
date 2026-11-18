package com.marabagam.art.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class CulturalEventDto {
    private UUID id;
    private String title;
    private String titleTa;
    private String slug;
    private Long districtId;
    private String districtName;
    private String districtNameTa;
    private UUID artFormId;
    private String artFormName;
    private String artFormNameTa;
    private String description;
    private String descriptionTa;
    private String eventType;
    private String eventPeriod;
    private String eventPeriodTa;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isDateApproximate;
    private String venueName;
    private String venueNameTa;
    private Double latitude;
    private Double longitude;
    private String provenanceSource;
    private String status;
    private UUID createdById;
    private String createdByName;
    private Instant createdAt;

    private Integer capacity;
    private Integer registeredCount = 0;
    private Instant registrationDeadline;
    private Boolean registrationRequired = false;
    private Boolean isOnline = false;
    private String meetingUrl;
    private String organizerName;
    private UUID instructorId;
    private String instructorName;
    private Instant startAt;
    private Instant endAt;
    private String timezoneIdentifier;
    private Boolean isUserRegistered = false;

    public CulturalEventDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictNameTa() {
        return districtNameTa;
    }

    public void setDistrictNameTa(String districtNameTa) {
        this.districtNameTa = districtNameTa;
    }

    public UUID getArtFormId() {
        return artFormId;
    }

    public void setArtFormId(UUID artFormId) {
        this.artFormId = artFormId;
    }

    public String getArtFormName() {
        return artFormName;
    }

    public void setArtFormName(String artFormName) {
        this.artFormName = artFormName;
    }

    public String getArtFormNameTa() {
        return artFormNameTa;
    }

    public void setArtFormNameTa(String artFormNameTa) {
        this.artFormNameTa = artFormNameTa;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(Integer registeredCount) {
        this.registeredCount = registeredCount;
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

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
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

    public Boolean getIsUserRegistered() {
        return isUserRegistered;
    }

    public void setIsUserRegistered(Boolean isUserRegistered) {
        this.isUserRegistered = isUserRegistered;
    }
}
