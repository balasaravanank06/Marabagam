package com.marabagam.notification.scheduler;

import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.domain.EventRegistration;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
import com.marabagam.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class EventReminderScheduler {

    private final CulturalEventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;

    public EventReminderScheduler(CulturalEventRepository eventRepository,
                                  EventRegistrationRepository registrationRepository,
                                  NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
    }

    public void sendUpcomingEventReminders() {
        sendEvent24HourReminders();
    }

    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    @Transactional
    public void sendEvent24HourReminders() {

        Instant now = Instant.now();
        Instant windowStart = now.plus(23, ChronoUnit.HOURS).plus(45, ChronoUnit.MINUTES);
        Instant windowEnd = now.plus(24, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES);

        List<CulturalEvent> upcomingEvents = eventRepository.findUpcomingEventsInWindow("PUBLISHED", windowStart, windowEnd);

        for (CulturalEvent event : upcomingEvents) {
            List<EventRegistration> registrations = registrationRepository.findByEventIdAndStatus(event.getId(), "REGISTERED");
            String dateStr = event.getStartAt() != null ?
                    event.getStartAt().atZone(ZoneId.of(event.getTimezoneIdentifier() != null ? event.getTimezoneIdentifier() : "Asia/Kolkata")).toLocalDate().toString() :
                    LocalDate.now().toString();

            for (EventRegistration reg : registrations) {
                String dedupKey = "EVENT_REMINDER|" + event.getId() + "|" + reg.getUser().getId() + "|" + dateStr;
                notificationService.sendNotification(
                        reg.getUser(),
                        "EVENT_REMINDER",
                        "Reminder: Event Starts in 24 Hours",
                        "நினைவூட்டல்: 24 மணிநேரத்தில் நிகழ்வு தொடங்குகிறது",
                        "Your registered event '" + event.getTitle() + "' begins in approximately 24 hours.",
                        "நீங்கள் பதிவு செய்த '" + event.getTitleTa() + "' நிகழ்வு 24 மணிநேரத்தில் தொடங்குகிறது.",
                        dedupKey,
                        "EVENT",
                        event.getId()
                );
            }
        }
    }
}
