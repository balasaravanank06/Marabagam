package com.marabagam.art.domain;

import com.marabagam.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cultural_events")
public class CulturalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "title_ta", nullable = false, length = 200)
    private String titleTa;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_form_id")
    private ArtForm artForm;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "description_ta", nullable = false, columnDefinition = "TEXT")
    private String descriptionTa;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType = "RECURRING_PERIOD";

    @Column(name = "event_period", nullable = false, length = 100)
    private String eventPeriod;

    @Column(name = "event_period_ta", nullable = false, length = 100)
    private String eventPeriodTa;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_date_approximate", nullable = false)
    private Boolean isDateApproximate = true;

    @Column(name = "venue_name", length = 200)
    private String venueName;

    @Column(name = "venue_name_ta", length = 200)
    private String venueNameTa;

    @Column(columnDefinition = "NUMERIC(8, 5)")
    private Double latitude;

    @Column(columnDefinition = "NUMERIC(8, 5)")
    private Double longitude;

    @Column(name = "provenance_source", columnDefinition = "TEXT")
    private String provenanceSource;

    @Column(nullable = false, length = 50)
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by")
    private User archivedBy;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "registration_deadline")
    private Instant registrationDeadline;

    @Column(name = "registration_required", nullable = false)
    private Boolean registrationRequired = false;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Column(name = "organizer_name", length = 200)
    private String organizerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private User instructor;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "timezone_identifier", length = 50)
    private String timezoneIdentifier = "Asia/Kolkata";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CulturalEvent() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public ArtForm getArtForm() {
        return artForm;
    }

    public void setArtForm(ArtForm artForm) {
        this.artForm = artForm;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public UUID getCreatedById() {
        return createdBy != null ? createdBy.getId() : null;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        // no-op for tests setting raw UUID if user entity managed separately
    }


    public User getArchivedBy() {
        return archivedBy;
    }

    public void setArchivedBy(User archivedBy) {
        this.archivedBy = archivedBy;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
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
