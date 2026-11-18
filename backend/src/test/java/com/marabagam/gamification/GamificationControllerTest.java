package com.marabagam.gamification;

import com.marabagam.auth.security.JwtTokenProvider;
import com.marabagam.gamification.domain.SourceActivity;
import com.marabagam.gamification.service.GamificationService;
import com.marabagam.user.domain.AccountStatus;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class GamificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private GamificationService gamificationService;

    private User learner;
    private String learnerToken;

    @BeforeEach
    void setUp() {
        learner = new User();
        learner.setEmail("game_learner_" + UUID.randomUUID() + "@marabagam.org");
        learner.setUsername("game_learner_" + UUID.randomUUID());
        learner.setPasswordHash("$2a$12$TestHashPasswordValueHere");
        learner.setDisplayName("Gamification Learner");
        learner.setRole(Role.LEARNER);
        learner.setAccountStatus(AccountStatus.ACTIVE);
        learner = userRepository.save(learner);

        learnerToken = tokenProvider.generateAccessToken(learner);

        gamificationService.awardXp(learner, 100, SourceActivity.LESSON_COMPLETED, "LESSON_1:" + learner.getId());
    }

    @Test
    void getProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalXp").value(100))
                .andExpect(jsonPath("$.data.level").value(2))
                .andExpect(jsonPath("$.data.levelTitleEn").value("Dedicated Practitioner"));
    }

    @Test
    void getLeaderboard_Public_Success() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
