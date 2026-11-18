package com.marabagam.art.service;

import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.domain.EventRegistration;
import com.marabagam.art.dto.EventRegistrationDto;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
import com.marabagam.common.audit.Auditable;
import com.marabagam.user.domain.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventRegistrationService {

    private final CulturalEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    public EventRegistrationService(CulturalEventRepository eventRepository,
                                    EventRegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    @Auditable(action = "REGISTER_EVENT", resourceType = "EVENT_REGISTRATION")
    public EventRegistrationDto registerForEvent(UUID eventId, User user) {
        // Pessimistic write lock on cultural_events row to prevent capacity overbooking
        CulturalEvent event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found with ID: " + eventId));

        if (!"PUBLISHED".equals(event.getStatus())) {
            throw new IllegalArgumentException("Cannot register for an unpublished or cancelled event");
        }

        if (event.getRegistrationDeadline() != null && Instant.now().isAfter(event.getRegistrationDeadline())) {
            throw new IllegalArgumentException("Registration deadline has passed");
        }

        // Check if user already has an active registration FIRST
        Optional<EventRegistration> activeOpt = registrationRepository.findByEventIdAndUserIdAndStatus(eventId, user.getId(), "REGISTERED");
        if (activeOpt.isPresent()) {
            throw new IllegalArgumentException("User is already registered for this event");
        }

        long activeCount = registrationRepository.countByEventIdAndStatus(eventId, "REGISTERED");
        if (event.getCapacity() != null && activeCount >= event.getCapacity()) {
            throw new IllegalStateException("Event capacity reached");
        }

        // Create new registration record (preserving history if past cancelled records exist)
        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setStatus("REGISTERED");

        try {
            EventRegistration saved = registrationRepository.save(registration);
            return toDto(saved);
        } catch (DataIntegrityViolationException e) {
            // Concurrent duplicate registration caught by partial unique index
            return registrationRepository.findByEventIdAndUserIdAndStatus(eventId, user.getId(), "REGISTERED")
                    .map(this::toDto)
                    .orElseThrow(() -> new IllegalStateException("Duplicate registration conflict"));
        }
    }

    @Transactional
    @Auditable(action = "CANCEL_REGISTRATION", resourceType = "EVENT_REGISTRATION")
    public EventRegistrationDto cancelRegistration(UUID eventId, User user) {
        EventRegistration registration = registrationRepository.findByEventIdAndUserIdAndStatus(eventId, user.getId(), "REGISTERED")
                .orElseThrow(() -> new NoSuchElementException("Active registration not found for event"));

        // Preserves database row for audit history
        registration.setStatus("CANCELLED");
        registration.setCancelledAt(Instant.now());

        EventRegistration saved = registrationRepository.save(registration);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationDto> getUserRegistrations(User user) {
        return registrationRepository.findByUserIdOrderByRegisteredAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private EventRegistrationDto toDto(EventRegistration reg) {
        EventRegistrationDto dto = new EventRegistrationDto();
        dto.setId(reg.getId());
        dto.setEventId(reg.getEvent().getId());
        dto.setEventTitle(reg.getEvent().getTitle());
        dto.setEventTitleTa(reg.getEvent().getTitleTa());
        dto.setUserId(reg.getUser().getId());
        dto.setUserName(reg.getUser().getFullName());
        dto.setStatus(reg.getStatus());
        dto.setRegisteredAt(reg.getRegisteredAt());
        dto.setCancelledAt(reg.getCancelledAt());
        return dto;
    }
}
