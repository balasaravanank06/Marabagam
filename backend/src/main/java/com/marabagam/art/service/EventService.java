package com.marabagam.art.service;

import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.domain.District;
import com.marabagam.art.domain.EventRegistration;
import com.marabagam.art.dto.CulturalEventDto;
import com.marabagam.art.dto.EventCreateDto;
import com.marabagam.art.dto.EventUpdateDto;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
import com.marabagam.common.audit.Auditable;
import com.marabagam.notification.service.NotificationService;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final CulturalEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final DistrictRepository districtRepository;
    private final ArtFormRepository artFormRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EventService(CulturalEventRepository eventRepository,
                        EventRegistrationRepository registrationRepository,
                        DistrictRepository districtRepository,
                        ArtFormRepository artFormRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.districtRepository = districtRepository;
        this.artFormRepository = artFormRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<CulturalEventDto> getPublicEvents(Long districtId, UUID artFormId, Pageable pageable, User currentUser) {
        Page<CulturalEvent> events;
        if (districtId != null) {
            events = eventRepository.findByDistrictIdAndStatus(districtId, "PUBLISHED", pageable);
        } else if (artFormId != null) {
            events = eventRepository.findByArtFormIdAndStatus(artFormId, "PUBLISHED", pageable);
        } else {
            events = eventRepository.findByStatus("PUBLISHED", pageable);
        }
        return events.map(event -> toDto(event, currentUser));
    }

    @Transactional(readOnly = true)
    public CulturalEventDto getEventBySlug(String slug, User currentUser) {
        CulturalEvent event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Event not found with slug: " + slug));

        if (!"PUBLISHED".equals(event.getStatus())) {
            if (currentUser == null) {
                throw new NoSuchElementException("Event not found with slug: " + slug);
            }
            boolean isOwner = currentUser != null && event.getCreatedBy() != null && event.getCreatedBy().getId() != null && event.getCreatedBy().getId().equals(currentUser.getId());
            boolean isGovRole = currentUser.getRole() != null &&
                    (currentUser.getRole().name().equals("ADMIN") ||
                     currentUser.getRole().name().equals("CULTURAL_EXPERT") ||
                     currentUser.getRole().name().equals("INSTRUCTOR") ||
                     currentUser.getRole().name().equals("ARTIST"));
            if (!isOwner && !isGovRole) {
                throw new NoSuchElementException("Event not found with slug: " + slug);
            }
        }

        return toDto(event, currentUser);
    }

    @Transactional
    @Auditable(action = "CREATE_EVENT", resourceType = "EVENT")
    public CulturalEventDto createEvent(EventCreateDto createDto, User creator) {
        validateEventData(createDto.getStartAt(), createDto.getEndAt(), createDto.getCapacity(),
                createDto.getRegistrationDeadline(), createDto.getRegistrationRequired(),
                createDto.getTimezoneIdentifier(), createDto.getIsOnline(), createDto.getMeetingUrl());

        District district = districtRepository.findById(createDto.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + createDto.getDistrictId()));

        ArtForm artForm = null;
        if (createDto.getArtFormId() != null) {
            artForm = artFormRepository.findById(createDto.getArtFormId()).orElse(null);
        }

        User instructor = null;
        if (createDto.getInstructorId() != null) {
            instructor = userRepository.findById(createDto.getInstructorId()).orElse(null);
        }

        CulturalEvent event = new CulturalEvent();
        event.setTitle(createDto.getTitle());
        event.setTitleTa(createDto.getTitleTa());
        event.setSlug(generateSlug(createDto.getTitle()));
        event.setDistrict(district);
        event.setArtForm(artForm);
        event.setDescription(createDto.getDescription());
        event.setDescriptionTa(createDto.getDescriptionTa());
        event.setEventType(createDto.getEventType() != null ? createDto.getEventType() : "RECURRING_PERIOD");
        event.setEventPeriod(createDto.getEventPeriod() != null ? createDto.getEventPeriod() : "Special Event");
        event.setEventPeriodTa(createDto.getEventPeriodTa() != null ? createDto.getEventPeriodTa() : "சிறப்பு நிகழ்வு");
        event.setIsDateApproximate(createDto.getIsDateApproximate() != null ? createDto.getIsDateApproximate() : false);
        event.setVenueName(createDto.getVenueName());
        event.setVenueNameTa(createDto.getVenueNameTa());
        event.setLatitude(createDto.getLatitude());
        event.setLongitude(createDto.getLongitude());
        event.setProvenanceSource(createDto.getProvenanceSource());
        event.setCapacity(createDto.getCapacity());
        event.setRegistrationDeadline(createDto.getRegistrationDeadline());
        event.setRegistrationRequired(createDto.getRegistrationRequired() != null ? createDto.getRegistrationRequired() : false);
        event.setIsOnline(createDto.getIsOnline() != null ? createDto.getIsOnline() : false);
        event.setMeetingUrl(createDto.getMeetingUrl());
        event.setOrganizerName(createDto.getOrganizerName() != null ? createDto.getOrganizerName() : creator.getFullName());
        event.setInstructor(instructor);
        event.setStartAt(createDto.getStartAt());
        event.setEndAt(createDto.getEndAt());
        event.setTimezoneIdentifier(createDto.getTimezoneIdentifier() != null ? createDto.getTimezoneIdentifier() : "Asia/Kolkata");
        event.setStatus("DRAFT");
        event.setCreatedBy(creator);

        CulturalEvent saved = eventRepository.save(event);
        return toDto(saved, creator);
    }

    @Transactional
    @Auditable(action = "UPDATE_EVENT", resourceType = "EVENT")
    public CulturalEventDto updateEvent(UUID eventId, EventUpdateDto updateDto, User currentUser) {
        CulturalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        // IDOR Defense: Only event creator or ADMIN/CULTURAL_EXPERT can edit
        boolean isOwner = currentUser != null && event.getCreatedBy() != null && event.getCreatedBy().getId() != null && event.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdminOrExpert = currentUser.getRole() != null &&
                (currentUser.getRole().name().equals("ADMIN") || currentUser.getRole().name().equals("CULTURAL_EXPERT"));
        if (!isOwner && !isAdminOrExpert) {
            throw new AccessDeniedException("You are not authorized to modify this event");
        }

        Instant newStartAt = updateDto.getStartAt() != null ? updateDto.getStartAt() : event.getStartAt();
        Instant newEndAt = updateDto.getEndAt() != null ? updateDto.getEndAt() : event.getEndAt();
        Integer newCapacity = updateDto.getCapacity() != null ? updateDto.getCapacity() : event.getCapacity();
        Instant newRegDeadline = updateDto.getRegistrationDeadline() != null ? updateDto.getRegistrationDeadline() : event.getRegistrationDeadline();
        Boolean newRegRequired = updateDto.getRegistrationRequired() != null ? updateDto.getRegistrationRequired() : event.getRegistrationRequired();
        String newTz = updateDto.getTimezoneIdentifier() != null ? updateDto.getTimezoneIdentifier() : event.getTimezoneIdentifier();
        Boolean newIsOnline = updateDto.getIsOnline() != null ? updateDto.getIsOnline() : event.getIsOnline();
        String newMeetingUrl = updateDto.getMeetingUrl() != null ? updateDto.getMeetingUrl() : event.getMeetingUrl();

        validateEventData(newStartAt, newEndAt, newCapacity, newRegDeadline, newRegRequired, newTz, newIsOnline, newMeetingUrl);

        boolean sensitiveChange = false;
        if ("PUBLISHED".equals(event.getStatus())) {
            if ((updateDto.getStartAt() != null && !updateDto.getStartAt().equals(event.getStartAt())) ||
                (updateDto.getEndAt() != null && !updateDto.getEndAt().equals(event.getEndAt())) ||
                (updateDto.getVenueName() != null && !updateDto.getVenueName().equals(event.getVenueName())) ||
                (updateDto.getInstructorId() != null && (event.getInstructor() == null || !updateDto.getInstructorId().equals(event.getInstructor().getId()))) ||
                (updateDto.getDescription() != null && !updateDto.getDescription().equals(event.getDescription()))) {
                sensitiveChange = true;
            }
        }

        if (updateDto.getTitle() != null) event.setTitle(updateDto.getTitle());
        if (updateDto.getTitleTa() != null) event.setTitleTa(updateDto.getTitleTa());
        if (updateDto.getDescription() != null) event.setDescription(updateDto.getDescription());
        if (updateDto.getDescriptionTa() != null) event.setDescriptionTa(updateDto.getDescriptionTa());
        if (updateDto.getVenueName() != null) event.setVenueName(updateDto.getVenueName());
        if (updateDto.getVenueNameTa() != null) event.setVenueNameTa(updateDto.getVenueNameTa());
        if (updateDto.getLatitude() != null) event.setLatitude(updateDto.getLatitude());
        if (updateDto.getLongitude() != null) event.setLongitude(updateDto.getLongitude());
        if (updateDto.getCapacity() != null) event.setCapacity(updateDto.getCapacity());
        if (updateDto.getRegistrationDeadline() != null) event.setRegistrationDeadline(updateDto.getRegistrationDeadline());
        if (updateDto.getRegistrationRequired() != null) event.setRegistrationRequired(updateDto.getRegistrationRequired());
        if (updateDto.getIsOnline() != null) event.setIsOnline(updateDto.getIsOnline());
        if (updateDto.getMeetingUrl() != null) event.setMeetingUrl(updateDto.getMeetingUrl());
        if (updateDto.getOrganizerName() != null) event.setOrganizerName(updateDto.getOrganizerName());
        if (updateDto.getStartAt() != null) event.setStartAt(updateDto.getStartAt());
        if (updateDto.getEndAt() != null) event.setEndAt(updateDto.getEndAt());
        if (updateDto.getTimezoneIdentifier() != null) event.setTimezoneIdentifier(updateDto.getTimezoneIdentifier());

        if (updateDto.getInstructorId() != null) {
            User instructor = userRepository.findById(updateDto.getInstructorId()).orElse(null);
            event.setInstructor(instructor);
        }

        if (sensitiveChange) {
            event.setStatus("PENDING_REVIEW");
        }

        CulturalEvent saved = eventRepository.save(event);
        return toDto(saved, currentUser);
    }

    @Transactional
    @Auditable(action = "SUBMIT_EVENT", resourceType = "EVENT")
    public CulturalEventDto submitEvent(UUID eventId, User currentUser) {
        CulturalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        boolean isOwner = currentUser != null && event.getCreatedBy() != null && event.getCreatedBy().getId() != null && event.getCreatedBy().getId().equals(currentUser.getId());
        if (!isOwner && !currentUser.getRole().name().equals("ADMIN")) {
            throw new AccessDeniedException("You are not authorized to submit this event");
        }

        event.setStatus("PENDING_REVIEW");
        return toDto(eventRepository.save(event), currentUser);
    }

    @Transactional
    @Auditable(action = "APPROVE_EVENT", resourceType = "EVENT")
    public CulturalEventDto approveEvent(UUID eventId, User currentUser) {
        CulturalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        // Self-approval prohibition: creator cannot approve own event
        if (event.getCreatedBy() != null && event.getCreatedBy().getId() != null && currentUser != null && event.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Self-approval prohibited: creator cannot approve their own event");
        }

        event.setStatus("APPROVED");
        return toDto(eventRepository.save(event), currentUser);
    }

    @Transactional
    @Auditable(action = "PUBLISH_EVENT", resourceType = "EVENT")
    public CulturalEventDto publishEvent(UUID eventId, User currentUser) {
        CulturalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        event.setStatus("PUBLISHED");
        return toDto(eventRepository.save(event), currentUser);
    }

    @Transactional
    @Auditable(action = "CANCEL_EVENT", resourceType = "EVENT")
    public CulturalEventDto cancelEvent(UUID eventId, User currentUser) {
        CulturalEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        boolean isOwner = currentUser != null && event.getCreatedBy() != null && event.getCreatedBy().getId() != null && event.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to cancel this event");
        }

        event.setStatus("CANCELLED");
        CulturalEvent saved = eventRepository.save(event);

        // Notify registered attendees
        List<EventRegistration> activeRegistrations = registrationRepository.findByEventIdAndStatus(eventId, "REGISTERED");
        for (EventRegistration reg : activeRegistrations) {
            notificationService.sendNotification(
                    reg.getUser(),
                    "EVENT_CANCELLED",
                    "Event Cancelled: " + event.getTitle(),
                    "நிகழ்வு ரத்து செய்யப்பட்டது: " + event.getTitleTa(),
                    "The event '" + event.getTitle() + "' scheduled for " + (event.getStartAt() != null ? event.getStartAt() : event.getEventPeriod()) + " has been cancelled.",
                    "நிகழ்வு '" + event.getTitleTa() + "' ரத்து செய்யப்பட்டுள்ளது.",
                    "EVENT_CANCELLED|" + eventId + "|" + reg.getUser().getId(),
                    "EVENT",
                    eventId
            );
        }

        return toDto(saved, currentUser);
    }

    private void validateEventData(Instant startAt, Instant endAt, Integer capacity, Instant deadline,
                                  Boolean regRequired, String tz, Boolean isOnline, String meetingUrl) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (capacity != null && capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero if specified");
        }
        if (deadline != null && startAt != null && deadline.isAfter(startAt)) {
            throw new IllegalArgumentException("Registration deadline cannot be after event start time");
        }
        if (capacity != null && capacity > 0 && Boolean.FALSE.equals(regRequired)) {
            throw new IllegalArgumentException("Registration must be required if capacity is specified");
        }
        if (tz != null) {
            try {
                ZoneId.of(tz);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid timezone identifier: " + tz);
            }
        }
        if (Boolean.TRUE.equals(isOnline) && meetingUrl != null && !meetingUrl.isBlank()) {
            if (!meetingUrl.toLowerCase().startsWith("https://")) {
                throw new IllegalArgumentException("Meeting URL must use HTTPS scheme");
            }
            if (meetingUrl.toLowerCase().contains("javascript:") || meetingUrl.toLowerCase().contains("data:")) {
                throw new IllegalArgumentException("Unsafe scheme in meeting URL");
            }
        }
    }

    public CulturalEventDto toDto(CulturalEvent event, User currentUser) {
        CulturalEventDto dto = new CulturalEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setTitleTa(event.getTitleTa());
        dto.setSlug(event.getSlug());
        if (event.getDistrict() != null) {
            dto.setDistrictId(event.getDistrict().getId());
            dto.setDistrictName(event.getDistrict().getName());
            dto.setDistrictNameTa(event.getDistrict().getNameTa());
        }
        if (event.getArtForm() != null) {
            dto.setArtFormId(event.getArtForm().getId());
            dto.setArtFormName(event.getArtForm().getName());
            dto.setArtFormNameTa(event.getArtForm().getNameTa());
        }
        dto.setDescription(event.getDescription());
        dto.setDescriptionTa(event.getDescriptionTa());
        dto.setEventType(event.getEventType());
        dto.setEventPeriod(event.getEventPeriod());
        dto.setEventPeriodTa(event.getEventPeriodTa());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setIsDateApproximate(event.getIsDateApproximate());
        dto.setVenueName(event.getVenueName());
        dto.setVenueNameTa(event.getVenueNameTa());
        dto.setLatitude(event.getLatitude());
        dto.setLongitude(event.getLongitude());
        dto.setProvenanceSource(event.getProvenanceSource());
        dto.setStatus(event.getStatus());
        if (event.getCreatedBy() != null) {
            dto.setCreatedById(event.getCreatedBy().getId());
            dto.setCreatedByName(event.getCreatedBy().getFullName());
        }
        dto.setCreatedAt(event.getCreatedAt());

        dto.setCapacity(event.getCapacity());
        long registered = registrationRepository.countByEventIdAndStatus(event.getId(), "REGISTERED");
        dto.setRegisteredCount((int) registered);
        dto.setRegistrationDeadline(event.getRegistrationDeadline());
        dto.setRegistrationRequired(event.getRegistrationRequired());
        dto.setIsOnline(event.getIsOnline());
        dto.setOrganizerName(event.getOrganizerName());
        if (event.getInstructor() != null) {
            dto.setInstructorId(event.getInstructor().getId());
            dto.setInstructorName(event.getInstructor().getFullName());
        }
        dto.setStartAt(event.getStartAt());
        dto.setEndAt(event.getEndAt());
        dto.setTimezoneIdentifier(event.getTimezoneIdentifier());

        // Private meeting URL security: expose meetingUrl ONLY to registered attendees
        boolean isRegistered = false;
        if (currentUser != null) {
            isRegistered = registrationRepository.existsByEventIdAndUserIdAndStatus(event.getId(), currentUser.getId(), "REGISTERED");
        }
        dto.setIsUserRegistered(isRegistered);

        if (isRegistered || (currentUser != null && event.getCreatedBy() != null && event.getCreatedBy().getId() != null && event.getCreatedBy().getId().equals(currentUser.getId()))) {
            dto.setMeetingUrl(event.getMeetingUrl());
        } else {
            dto.setMeetingUrl(null); // Private URL hidden from non-registered users
        }

        return dto;
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (eventRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }
}
