package com.marabagam.movement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.art.domain.ArtForm;
import com.marabagam.art.domain.ArtFormStatus;
import com.marabagam.art.repository.ArtFormRepository;
import com.marabagam.movement.domain.*;
import com.marabagam.movement.dto.*;
import com.marabagam.movement.repository.*;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MovementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArtFormRepository artFormRepository;

    @Autowired
    private MovementRepository movementRepository;

    @Autowired
    private MovementVersionRepository versionRepository;

    @Autowired
    private ReferenceMovementRepository referenceRepository;

    @Autowired
    private PracticeSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.marabagam.auth.security.JwtTokenProvider tokenProvider;

    private User learner1;
    private User learner2;
    private User artistUser;
    private User expertUser;
    private Movement silambamStance;
    private MovementVersion stanceVersion;

    private String getAccessToken(User user) {
        return tokenProvider.generateAccessToken(user);
    }

    private User createUser(String email, String username, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash("hashed_pw");
        user.setDisplayName(username);
        user.setRole(role);
        return userRepository.save(user);
    }

    @BeforeEach
    void setUp() {
        learner1 = createUser("learner1_p7@test.com", "learner1_p7", Role.LEARNER);
        learner2 = createUser("learner2_p7@test.com", "learner2_p7", Role.LEARNER);
        artistUser = createUser("artist_p7@test.com", "artist_p7", Role.ARTIST);
        expertUser = createUser("expert_p7@test.com", "expert_p7", Role.CULTURAL_EXPERT);

        ArtForm silambam = new ArtForm();
        silambam.setName("Silambam Test");
        silambam.setNameTa("சிலம்பம் டெஸ்ட்");
        silambam.setSlug("silambam-test-p7-" + UUID.randomUUID().toString().substring(0, 8));
        silambam.setShortDescription("Staff martial art");
        silambam.setShortDescriptionTa("தற்காப்பு கலை");
        silambam.setDescription("Ancient martial art");
        silambam.setDescriptionTa("பண்டைய கலை");
        silambam.setOrigin("Tamil Nadu");
        silambam.setOriginTa("தமிழ்நாடு");
        silambam.setStatus(ArtFormStatus.PUBLISHED);
        silambam = artFormRepository.save(silambam);

        silambamStance = new Movement();
        silambamStance.setArtForm(silambam);
        silambamStance.setCode("SILAMBAM_STANCE_TEST_" + UUID.randomUUID().toString().substring(0, 8));
        silambamStance.setNameEn("Maankombu Subadu");
        silambamStance.setNameTa("மான்கொம்பு சுவடு");
        silambamStance.setCategory("STANCE");
        silambamStance.setDifficultyLevel("BEGINNER");
        silambamStance.setIsActive(true);
        silambamStance = movementRepository.save(silambamStance);

        stanceVersion = new MovementVersion();
        stanceVersion.setMovement(silambamStance);
        stanceVersion.setVersionNumber("v1.0.0");
        stanceVersion.setJointRulesJson("{\"type\":\"STATIC\",\"rules\":{}}");
        stanceVersion.setIsCurrent(true);
        stanceVersion = versionRepository.save(stanceVersion);
    }

    @Test
    @DisplayName("Public Movement Catalog Endpoint - List Active Movements")
    void testGetPublicMovements() throws Exception {
        mockMvc.perform(get("/api/v1/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Unauthenticated User - Rejected from Practice Sessions")
    void testUnauthenticatedAccessRejected() throws Exception {
        mockMvc.perform(post("/api/v1/practice-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movementId\":\"" + silambamStance.getId() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Learner Starts Practice Session")
    void testStartPracticeSession() throws Exception {
        StartPracticeSessionRequestDto req = new StartPracticeSessionRequestDto();
        req.setMovementId(silambamStance.getId());

        mockMvc.perform(post("/api/v1/practice-sessions")
                        .header("Authorization", "Bearer " + getAccessToken(learner1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.movementId", is(silambamStance.getId().toString())))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("Evaluate Practice Session & Validate Frame Coordinates")
    void testEvaluatePracticeSession() throws Exception {
        PracticeSession session = new PracticeSession();
        session.setUser(learner1);
        session.setMovement(silambamStance);
        session.setVersion(stanceVersion);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session = sessionRepository.save(session);

        EvaluatePracticeSessionRequestDto evalReq = new EvaluatePracticeSessionRequestDto();
        List<FrameLandmarkDto> frames = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            FrameLandmarkDto frame = new FrameLandmarkDto();
            frame.setFrameIndex(i);
            frame.setTimestampMs((long) (i * 33));
            List<LandmarkDto> landmarks = new ArrayList<>();
            for (int j = 0; j < 33; j++) {
                landmarks.add(new LandmarkDto(0.5, 0.5, 0.0, 0.95, 0.95));
            }
            frame.setLandmarks(landmarks);
            frames.add(frame);
        }
        evalReq.setFrames(frames);

        mockMvc.perform(post("/api/v1/practice-sessions/" + session.getId() + "/evaluate")
                        .header("Authorization", "Bearer " + getAccessToken(learner1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.overallScore", notNullValue()));
    }

    @Test
    @DisplayName("Input Validation - Reject Malformed Coordinates (x > 1.0)")
    void testRejectInvalidCoordinates() throws Exception {
        PracticeSession session = new PracticeSession();
        session.setUser(learner1);
        session.setMovement(silambamStance);
        session.setVersion(stanceVersion);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session = sessionRepository.save(session);

        EvaluatePracticeSessionRequestDto evalReq = new EvaluatePracticeSessionRequestDto();
        List<FrameLandmarkDto> frames = new ArrayList<>();
        FrameLandmarkDto frame = new FrameLandmarkDto();
        frame.setFrameIndex(0);
        List<LandmarkDto> landmarks = new ArrayList<>();
        landmarks.add(new LandmarkDto(1.5, 0.5, 0.0, 0.95, 0.95)); // Out of bounds x = 1.5
        frame.setLandmarks(landmarks);
        frames.add(frame);
        evalReq.setFrames(frames);

        mockMvc.perform(post("/api/v1/practice-sessions/" + session.getId() + "/evaluate")
                        .header("Authorization", "Bearer " + getAccessToken(learner1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(evalReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("IDOR Protection - Learner 2 Cannot Access or Delete Learner 1 Session")
    void testIdorProtection() throws Exception {
        PracticeSession session = new PracticeSession();
        session.setUser(learner1);
        session.setMovement(silambamStance);
        session.setVersion(stanceVersion);
        session.setStatus(SessionStatus.IN_PROGRESS);
        session = sessionRepository.save(session);

        // Learner 2 tries to GET Learner 1's session -> ResourceNotFound / Access Denied
        mockMvc.perform(get("/api/v1/practice-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + getAccessToken(learner2)))
                .andExpect(status().isNotFound());

        // Learner 2 tries to DELETE Learner 1's session -> ResourceNotFound / Access Denied
        mockMvc.perform(delete("/api/v1/practice-sessions/" + session.getId())
                        .header("Authorization", "Bearer " + getAccessToken(learner2)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Reference Movement Governance - Artist Submits Reference Movement")
    void testArtistSubmitReference() throws Exception {
        SubmitReferenceRequestDto req = new SubmitReferenceRequestDto();
        req.setTitle("Master Silambam Stance Demo");
        req.setPerformerAttribution("Master Ramanathan");
        req.setKeypointsTemplateJson("[{}]");

        mockMvc.perform(post("/api/v1/admin/movements/" + silambamStance.getId() + "/references")
                        .header("Authorization", "Bearer " + getAccessToken(artistUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PENDING_REVIEW")));
    }

    @Test
    @DisplayName("Self-Approval Prohibition Check - Artist Cannot Approve Own Reference")
    void testSelfApprovalProhibited() throws Exception {
        ReferenceMovement ref = new ReferenceMovement();
        ref.setMovement(silambamStance);
        ref.setVersion(stanceVersion);
        ref.setTitle("Artist Demo");
        ref.setPerformerAttribution("Self");
        ref.setKeypointsTemplateJson("{}");
        ref.setStatus(ReferenceStatus.PENDING_REVIEW);
        ref.setCreatedBy(artistUser);
        ref = referenceRepository.save(ref);

        // Artist tries to approve own reference -> 403 Forbidden / Access Denied
        mockMvc.perform(put("/api/v1/admin/references/" + ref.getId() + "/approve")
                        .header("Authorization", "Bearer " + getAccessToken(artistUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Reference Movement Approval - Cultural Expert Approves Candidate")
    void testCulturalExpertApproveReference() throws Exception {
        ReferenceMovement ref = new ReferenceMovement();
        ref.setMovement(silambamStance);
        ref.setVersion(stanceVersion);
        ref.setTitle("Artist Demo");
        ref.setPerformerAttribution("Master Murugan");
        ref.setKeypointsTemplateJson("{}");
        ref.setStatus(ReferenceStatus.PENDING_REVIEW);
        ref.setCreatedBy(artistUser); // Created by artist, approved by expert
        ref = referenceRepository.save(ref);

        mockMvc.perform(put("/api/v1/admin/references/" + ref.getId() + "/approve")
                        .header("Authorization", "Bearer " + getAccessToken(expertUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("APPROVED")))
                .andExpect(jsonPath("$.data.approvedByName", is("expert_p7")));
    }
}
