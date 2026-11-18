package com.marabagam.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.ai.domain.*;
import com.marabagam.ai.dto.*;
import com.marabagam.ai.repository.AiConversationRepository;
import com.marabagam.ai.repository.KnowledgeDocumentRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AiGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeDocumentRepository documentRepository;

    @Autowired
    private AiConversationRepository conversationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.marabagam.auth.security.JwtTokenProvider tokenProvider;

    private User learner1;
    private User learner2;
    private User artistUser;
    private User expertUser;
    private User adminUser;

    @BeforeEach
    public void setUp() {
        conversationRepository.deleteAll();
        documentRepository.deleteAll();
        userRepository.deleteAll();

        learner1 = createUser("learner1_ai@marabagam.org", "learner1_ai", Role.LEARNER);
        learner2 = createUser("learner2_ai@marabagam.org", "learner2_ai", Role.LEARNER);
        artistUser = createUser("artist_ai@marabagam.org", "artist_ai", Role.ARTIST);
        expertUser = createUser("expert_ai@marabagam.org", "expert_ai", Role.CULTURAL_EXPERT);
        adminUser = createUser("admin_ai@marabagam.org", "admin_ai", Role.ADMIN);
    }

    private User createUser(String email, String username, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode("Password123!"));
        user.setDisplayName("Test " + username);
        user.setPreferredLanguage("en");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return userRepository.save(user);
    }

    private String getToken(User user) {
        return tokenProvider.generateAccessToken(user);
    }

    // 1. GUEST request rejected (401)
    @Test
    public void testAskCulturalGuide_GuestRejected() throws Exception {
        AskCulturalGuideRequestDto req = new AskCulturalGuideRequestDto();
        req.setQuery("What is Silambam?");

        mockMvc.perform(post("/api/v1/ai/cultural-guide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // 2. Authenticated LEARNER request succeeds
    @Test
    public void testAskCulturalGuide_AuthenticatedLearnerSuccess() throws Exception {
        String token = getToken(learner1);

        AskCulturalGuideRequestDto req = new AskCulturalGuideRequestDto();
        req.setQuery("What is Silambam?");

        mockMvc.perform(post("/api/v1/ai/cultural-guide")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.answer").exists());
    }

    // 3. Excessive query length rejected (>1000 chars)
    @Test
    public void testAskCulturalGuide_ExcessiveQueryLengthRejected() throws Exception {
        String token = getToken(learner1);

        AskCulturalGuideRequestDto req = new AskCulturalGuideRequestDto();
        req.setQuery("A".repeat(1005));

        mockMvc.perform(post("/api/v1/ai/cultural-guide")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // 4. Cross-user conversation access denied (IDOR protection)
    @Test
    public void testConversationOwnership_CrossUserAccessDenied() throws Exception {
        String token1 = getToken(learner1);
        String token2 = getToken(learner2);

        // Learner 1 asks a question
        AskCulturalGuideRequestDto req = new AskCulturalGuideRequestDto();
        req.setQuery("What is Silambam?");

        String responseJson = mockMvc.perform(post("/api/v1/ai/cultural-guide")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String convIdStr = objectMapper.readTree(responseJson).get("data").get("conversationId").asText();
        UUID convId = UUID.fromString(convIdStr);

        // Learner 2 attempts to fetch Learner 1's conversation -> 404 / access denied
        mockMvc.perform(get("/api/v1/ai/conversations/" + convId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }

    // 5. Hard conversation deletion
    @Test
    public void testDeleteConversation_HardDeleteSemantics() throws Exception {
        String token = getToken(learner1);

        AskCulturalGuideRequestDto req = new AskCulturalGuideRequestDto();
        req.setQuery("What is Silambam?");

        String responseJson = mockMvc.perform(post("/api/v1/ai/cultural-guide")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String convIdStr = objectMapper.readTree(responseJson).get("data").get("conversationId").asText();
        UUID convId = UUID.fromString(convIdStr);

        // Learner 1 hard deletes conversation
        mockMvc.perform(delete("/api/v1/ai/conversations/" + convId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify conversation is completely removed from DB
        assertFalse(conversationRepository.findById(convId).isPresent());
    }

    // 6. Creator ownership & governance: Artist cannot approve own knowledge draft
    @Test
    public void testArtistCannotApproveOwnKnowledgeDraft() throws Exception {
        String artistToken = getToken(artistUser);

        CreateKnowledgeDocumentRequestDto createReq = new CreateKnowledgeDocumentRequestDto();
        createReq.setTitle("Draft Heritage Note");
        createReq.setContent("Draft content");
        createReq.setSourceName("Personal Notes");
        createReq.setSourceType(SourceType.ARTICLE);

        String docJson = mockMvc.perform(post("/api/v1/admin/knowledge-documents")
                        .header("Authorization", "Bearer " + artistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String docIdStr = objectMapper.readTree(docJson).get("data").get("id").asText();
        UUID docId = UUID.fromString(docIdStr);

        // Artist attempts to set status to APPROVED -> 403 Forbidden
        UpdateKnowledgeStatusRequestDto statusReq = new UpdateKnowledgeStatusRequestDto();
        statusReq.setStatus(DocumentStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/admin/knowledge-documents/" + docId + "/status")
                        .header("Authorization", "Bearer " + artistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isForbidden());
    }

    // 7. Cultural Expert can approve knowledge draft
    @Test
    public void testCulturalExpertCanApproveKnowledgeDraft() throws Exception {
        String artistToken = getToken(artistUser);
        String expertToken = getToken(expertUser);

        CreateKnowledgeDocumentRequestDto createReq = new CreateKnowledgeDocumentRequestDto();
        createReq.setTitle("Draft Heritage Note 2");
        createReq.setContent("Draft content 2");
        createReq.setSourceName("Personal Notes");
        createReq.setSourceType(SourceType.ARTICLE);

        String docJson = mockMvc.perform(post("/api/v1/admin/knowledge-documents")
                        .header("Authorization", "Bearer " + artistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String docIdStr = objectMapper.readTree(docJson).get("data").get("id").asText();
        UUID docId = UUID.fromString(docIdStr);

        // Cultural Expert approves -> 200 OK
        UpdateKnowledgeStatusRequestDto statusReq = new UpdateKnowledgeStatusRequestDto();
        statusReq.setStatus(DocumentStatus.APPROVED);

        mockMvc.perform(patch("/api/v1/admin/knowledge-documents/" + docId + "/status")
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}
