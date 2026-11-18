package com.marabagam.art;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.*;
import com.marabagam.art.dto.EventCreateDto;
import com.marabagam.art.dto.EventUpdateDto;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.auth.dto.LoginRequestDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CulturalEventRepository eventRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User artist1;
    private User artist2;
    private User expert;
    private User admin;
    private District maduraiDistrict;

    private String artist1Token;
    private String artist2Token;
    private String expertToken;

    @BeforeEach
    public void setUp() throws Exception {
        eventRepository.deleteAll();
        districtRepository.deleteAll();
        userRepository.deleteAll();

        maduraiDistrict = new District();
        maduraiDistrict.setName("Madurai");
        maduraiDistrict.setNameTa("மதுரை");
        maduraiDistrict = districtRepository.save(maduraiDistrict);

        artist1 = createUser("artist1@test.com", "artist1", "Artist One", Role.ARTIST);
        artist2 = createUser("artist2@test.com", "artist2", "Artist Two", Role.ARTIST);

        expert = createUser("expert@test.com", "expert", "Expert User", Role.CULTURAL_EXPERT);
        admin = createUser("admin@test.com", "admin", "Admin User", Role.ADMIN);

        artist1Token = loginAndGetToken("artist1@test.com", "password123");
        artist2Token = loginAndGetToken("artist2@test.com", "password123");
        expertToken = loginAndGetToken("expert@test.com", "password123");
    }

    private User createUser(String email, String username, String name, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setUsername(username);
        u.setDisplayName(name);
        u.setPasswordHash(passwordEncoder.encode("password123"));
        u.setRole(role);
        u.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(u);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsernameOrEmail(email);
        request.setPassword(password);
        MvcResult res = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        String json = res.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("data").get("accessToken").asText();
    }

    @Test
    public void testPublicApiReturnsOnlyPublishedEvents() throws Exception {
        CulturalEvent pub = new CulturalEvent();
        pub.setTitle("Published Silambam Fest");
        pub.setTitleTa("சிலம்பம் திருவிழா");
        pub.setSlug("pub-silambam");
        pub.setDescription("Public event description");
        pub.setDescriptionTa("பொது நிகழ்வு விளக்கம்");
        pub.setEventPeriod("Special Event");
        pub.setEventPeriodTa("சிறப்பு நிகழ்வு");
        pub.setStatus("PUBLISHED");
        pub.setDistrict(maduraiDistrict);
        pub.setCreatedBy(artist1);
        eventRepository.save(pub);

        CulturalEvent draft = new CulturalEvent();
        draft.setTitle("Draft Event");
        draft.setTitleTa("வரைவு நிகழ்வு");
        draft.setSlug("draft-event");
        draft.setDescription("Draft description");
        draft.setDescriptionTa("வரைவு விளக்கம்");
        draft.setEventPeriod("Special Event");
        draft.setEventPeriodTa("சிறப்பு நிகழ்வு");
        draft.setStatus("DRAFT");
        draft.setDistrict(maduraiDistrict);
        draft.setCreatedBy(artist1);
        eventRepository.save(draft);


        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].slug").value("pub-silambam"));
    }

    @Test
    public void testCreateEventAndGovernanceLifecycle() throws Exception {
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle("Karagattam Workshop");
        dto.setTitleTa("கரகாட்டம் பட்டறை");
        dto.setDescription("Learn Karagattam dance step by step.");
        dto.setDescriptionTa("கரகாட்ட நடனத்தைக் கற்றுக்கொள்ளுங்கள்.");
        dto.setDistrictId(maduraiDistrict.getId());
        dto.setStartAt(Instant.now().plus(7, ChronoUnit.DAYS));
        dto.setEndAt(Instant.now().plus(7, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS));
        dto.setTimezoneIdentifier("Asia/Kolkata");
        dto.setCapacity(30);
        dto.setRegistrationRequired(true);
        dto.setIsOnline(true);
        dto.setMeetingUrl("https://meet.marabagam.com/karagattam");

        MvcResult res = mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + artist1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        String eventId = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("id").asText();

        // Submit for review
        mockMvc.perform(put("/api/v1/events/" + eventId + "/submit")
                .header("Authorization", "Bearer " + artist1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_REVIEW"));

        // Creator (ARTIST) attempting to approve must fail with 403 FORBIDDEN (RBAC enforcement)
        mockMvc.perform(put("/api/v1/events/" + eventId + "/approve")
                .header("Authorization", "Bearer " + artist1Token))
                .andExpect(status().isForbidden());

        // Expert approves
        mockMvc.perform(put("/api/v1/events/" + eventId + "/approve")
                .header("Authorization", "Bearer " + expertToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Expert publishes
        mockMvc.perform(put("/api/v1/events/" + eventId + "/publish")
                .header("Authorization", "Bearer " + expertToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    public void testIDORPreventionOnEventEditing() throws Exception {
        CulturalEvent event = new CulturalEvent();
        event.setTitle("Artist 1 Event");
        event.setTitleTa("கலைஞர் 1 நிகழ்வு");
        event.setSlug("artist-1-event");
        event.setDescription("Description");
        event.setDescriptionTa("விளக்கம்");
        event.setEventPeriod("Special Event");
        event.setEventPeriodTa("சிறப்பு நிகழ்வு");
        event.setStatus("DRAFT");
        event.setDistrict(maduraiDistrict);
        event.setCreatedBy(artist1);
        event = eventRepository.save(event);


        EventUpdateDto updateDto = new EventUpdateDto();
        updateDto.setTitle("Hacked Title");

        // Artist 2 attempts to edit Artist 1's event -> 403 FORBIDDEN
        mockMvc.perform(put("/api/v1/events/" + event.getId())
                .header("Authorization", "Bearer " + artist2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testValidationRules() throws Exception {
        // Invalid Timezone
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle("Invalid Timezone Event");
        dto.setTitleTa("தவறான நேர மண்டலம்");
        dto.setDescription("Description");
        dto.setDescriptionTa("விளக்கம்");
        dto.setDistrictId(maduraiDistrict.getId());
        dto.setTimezoneIdentifier("Invalid/Timezone_Name");

        mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + artist1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        // End before start
        dto.setTimezoneIdentifier("Asia/Kolkata");
        dto.setStartAt(Instant.now().plus(5, ChronoUnit.DAYS));
        dto.setEndAt(Instant.now().plus(4, ChronoUnit.DAYS));
        mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + artist1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        // Capacity <= 0
        dto.setEndAt(Instant.now().plus(6, ChronoUnit.DAYS));
        dto.setCapacity(0);
        mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + artist1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        // Insecure Meeting URL (http://)
        dto.setCapacity(50);
        dto.setIsOnline(true);
        dto.setMeetingUrl("http://insecure-meeting.com");
        mockMvc.perform(post("/api/v1/events")
                .header("Authorization", "Bearer " + artist1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMeetingUrlIsMaskedForNonRegisteredUsers() throws Exception {
        CulturalEvent pub = new CulturalEvent();
        pub.setTitle("Online Workshop");
        pub.setTitleTa("இணையப் பட்டறை");
        pub.setSlug("online-workshop");
        pub.setDescription("Description");
        pub.setDescriptionTa("விளக்கம்");
        pub.setEventPeriod("Special Event");
        pub.setEventPeriodTa("சிறப்பு நிகழ்வு");
        pub.setStatus("PUBLISHED");
        pub.setDistrict(maduraiDistrict);
        pub.setIsOnline(true);
        pub.setMeetingUrl("https://meet.marabagam.com/secret-room");
        pub.setCreatedBy(artist1);
        pub = eventRepository.save(pub);


        mockMvc.perform(get("/api/v1/events/" + pub.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingUrl").doesNotExist());
    }
}
