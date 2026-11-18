package com.marabagam.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.CulturalEvent;
import com.marabagam.art.domain.EventRegistration;

import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.notification.domain.Notification;
import com.marabagam.notification.domain.NotificationCategory;
import com.marabagam.notification.domain.NotificationPreference;
import com.marabagam.notification.repository.NotificationPreferenceRepository;
import com.marabagam.notification.repository.NotificationRepository;
import com.marabagam.notification.scheduler.EventReminderScheduler;
import com.marabagam.notification.service.NotificationService;
import com.marabagam.user.domain.AccountStatus;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EventReminderScheduler reminderScheduler;

    @Autowired
    private CulturalEventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private com.marabagam.art.repository.DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User recipient;
    private com.marabagam.art.domain.District testDistrict;
    private String recipientToken;

    @BeforeEach
    public void setUp() throws Exception {
        notificationRepository.deleteAll();
        preferenceRepository.deleteAll();
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        recipient = new User();
        recipient.setEmail("recipient@test.com");
        recipient.setUsername("recipient");
        recipient.setDisplayName("Notification Recipient");
        recipient.setPasswordHash(passwordEncoder.encode("password123"));
        recipient.setRole(Role.LEARNER);
        recipient.setAccountStatus(AccountStatus.ACTIVE);
        recipient = userRepository.save(recipient);

        testDistrict = new com.marabagam.art.domain.District();
        testDistrict.setName("Chennai Notification District");
        testDistrict.setNameTa("சென்னை");
        testDistrict.setCode("CHE_NOTIF");
        testDistrict = districtRepository.save(testDistrict);

        LoginRequestDto req = new LoginRequestDto();
        req.setUsernameOrEmail("recipient@test.com");
        req.setPassword("password123");
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        recipientToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    public void testNotificationLifecycleAndUnreadCount() throws Exception {
        // Create 2 notifications for recipient
        notificationService.sendNotification(
                recipient,
                NotificationCategory.BADGE_EARNED,
                "Badge Earned!",
                "பேட்ஜ் வென்றீர்கள்!",
                "You earned Silambam Novice",
                "சிலம்பம் தொடக்கநிலை பேட்ஜ் பெற்றீர்கள்",
                "badge_dedup_1",
                null,
                null
        );

        notificationService.sendNotification(
                recipient,
                NotificationCategory.COMMUNITY_REPLY,
                "New Reply",
                "புதிய மறுமொழி",
                "User commented on your post",
                "உங்கள் பதிவில் கருத்து தெரிவித்தனர்",
                "reply_dedup_1",
                null,
                null
        );

        // Deduplication test: sending duplicate deduplication_key for recipient should not create 3rd row
        notificationService.sendNotification(
                recipient,
                NotificationCategory.COMMUNITY_REPLY,
                "New Reply Duplicate",
                "புதிய மறுமொழி",
                "User commented on your post",
                "உங்கள் பதிவில் கருத்து தெரிவித்தனர்",
                "reply_dedup_1",
                null,
                null
        );

        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
        assertEquals(2, list.size());

        // Check unread count API -> 2
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                .header("Authorization", "Bearer " + recipientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(2));

        // Mark all as read
        mockMvc.perform(put("/api/v1/notifications/read-all")
                .header("Authorization", "Bearer " + recipientToken))
                .andExpect(status().isOk());

        // Check unread count API -> 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                .header("Authorization", "Bearer " + recipientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    public void testEventReminderSchedulerIdempotency() throws Exception {
        // Create an event scheduled in 24 hours (23.5 to 24.5 hrs)
        CulturalEvent event = new CulturalEvent();
        event.setTitle("Tomorrow's Workshop");
        event.setTitleTa("நாளை பட்டறை");
        event.setSlug("tomorrow-workshop");
        event.setDistrict(testDistrict);
        event.setDescription("Workshop description");
        event.setDescriptionTa("பட்டறை விளக்கம்");
        event.setEventPeriod("Period");
        event.setEventPeriodTa("காலம்");
        event.setStatus("PUBLISHED");
        event.setStartAt(Instant.now().plus(24, ChronoUnit.HOURS));
        event.setCreatedBy(recipient);
        event = eventRepository.save(event);

        EventRegistration reg = new EventRegistration();
        reg.setEvent(event);
        reg.setUser(recipient);
        reg.setStatus("REGISTERED");
        registrationRepository.save(reg);


        // Trigger reminder scheduler
        reminderScheduler.sendUpcomingEventReminders();

        // Check 1 notification created
        List<Notification> notes1 = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
        assertEquals(1, notes1.size());
        assertEquals(NotificationCategory.EVENT_REMINDER, notes1.get(0).getCategory());

        // Trigger scheduler again -> should be idempotent, no duplicate notification created
        reminderScheduler.sendUpcomingEventReminders();
        List<Notification> notes2 = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
        assertEquals(1, notes2.size());
    }

    @Test
    public void testNotificationPreferenceDisabledCategory() throws Exception {
        // Disable COMMUNITY_REPLY category for recipient
        NotificationPreference pref = new NotificationPreference(recipient);
        pref.setCommunityEnabled(false);
        preferenceRepository.save(pref);

        notificationService.sendNotification(
                recipient,
                NotificationCategory.COMMUNITY_REPLY,
                "Suppressed Reply",
                "மறுமொழி",
                "Content",
                "உள்ளடக்கம்",
                "reply_suppressed",
                null,
                null
        );

        // Check 0 notifications created
        List<Notification> notes = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
        assertEquals(0, notes.size());

        // Attempting to disable ACCOUNT_SECURITY (non-configurable critical) should be ignored
        notificationService.sendNotification(
                recipient,
                NotificationCategory.ACCOUNT_SECURITY,
                "Security Alert",
                "பாதுகாப்பு எச்சரிக்கை",
                "New login detected",
                "புதிய உள்நுழைவு",
                "sec_alert_1",
                null,
                null
        );

        List<Notification> notesSec = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipient.getId());
        assertEquals(1, notesSec.size());
    }
}
