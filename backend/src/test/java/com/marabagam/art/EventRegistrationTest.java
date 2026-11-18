package com.marabagam.art;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.*;
import com.marabagam.art.repository.CulturalEventRepository;
import com.marabagam.art.repository.DistrictRepository;
import com.marabagam.art.repository.EventRegistrationRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CulturalEventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;
    private District district;
    private CulturalEvent event;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    public void setUp() throws Exception {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        districtRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        district = new District();
        district.setName("Thanjavur");
        district.setNameTa("தஞ்சாவூர்");
        district = districtRepository.save(district);

        user1 = createUser("user1@test.com", "user1", "Learner One", Role.LEARNER);
        user2 = createUser("user2@test.com", "user2", "Learner Two", Role.LEARNER);

        user1Token = loginAndGetToken("user1@test.com", "password123");
        user2Token = loginAndGetToken("user2@test.com", "password123");

        event = new CulturalEvent();
        event.setTitle("Bharatanatyam Masterclass");
        event.setTitleTa("பரதநாட்டியம் மாஸ்டர்கிளாஸ்");
        event.setSlug("bharatanatyam-masterclass");
        event.setDescription("Masterclass description");
        event.setDescriptionTa("மாஸ்டர்கிளாஸ் விளக்கம்");
        event.setEventPeriod("Special Event");
        event.setEventPeriodTa("சிறப்பு நிகழ்வு");
        event.setStatus("PUBLISHED");
        event.setDistrict(district);
        event.setCapacity(1); // Capacity 1 for testing race condition / last slot
        event.setRegistrationRequired(true);
        event.setStartAt(Instant.now().plus(3, ChronoUnit.DAYS));
        event.setRegistrationDeadline(Instant.now().plus(2, ChronoUnit.DAYS));
        event.setIsOnline(true);
        event.setMeetingUrl("https://meet.marabagam.com/bharatanatyam");
        event.setCreatedBy(user1);
        event = eventRepository.save(event);

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
    public void testRegistrationLifecycleAndCapacityLimit() throws Exception {
        // User 1 registers for event
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));

        // Meeting URL is now visible to registered user 1
        mockMvc.perform(get("/api/v1/events/" + event.getSlug())
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.meetingUrl").value("https://meet.marabagam.com/bharatanatyam"));

        // User 2 attempts to register, but capacity (1) is reached -> 409 CONFLICT
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isConflict());

        // User 1 cancels registration
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/cancel-registration")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // History preservation check: registration row still exists with CANCELLED status
        List<EventRegistration> regHistory = registrationRepository.findByUserId(user1.getId());
        assertEquals(1, regHistory.size());
        assertEquals("CANCELLED", regHistory.get(0).getStatus());

        // User 2 can now register since slot freed
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));
    }

    @Test
    public void testDuplicateActiveRegistrationRejection() throws Exception {
        // User 1 registers
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isCreated());

        // User 1 registers again while already registered -> 400 BAD_REQUEST
        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegistrationDeadlinePassedRejection() throws Exception {
        // Set deadline in past
        event.setRegistrationDeadline(Instant.now().minus(1, ChronoUnit.HOURS));
        eventRepository.save(event);

        mockMvc.perform(post("/api/v1/events/" + event.getId() + "/register")
                .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isBadRequest());
    }
}
