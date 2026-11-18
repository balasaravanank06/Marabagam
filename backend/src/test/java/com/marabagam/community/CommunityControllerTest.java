package com.marabagam.community;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.community.domain.*;
import com.marabagam.community.dto.CommunityCommentCreateDto;
import com.marabagam.community.dto.CommunityPostCreateDto;
import com.marabagam.community.dto.CommunityReportCreateDto;
import com.marabagam.community.repository.CommunityCommentRepository;
import com.marabagam.community.repository.CommunityPostRepository;
import com.marabagam.community.repository.CommunityReportRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommunityPostRepository postRepository;

    @Autowired
    private CommunityCommentRepository commentRepository;

    @Autowired
    private CommunityReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;
    private User moderator;
    private String user1Token;
    private String user2Token;
    private String moderatorToken;

    @BeforeEach
    public void setUp() throws Exception {
        reportRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        user1 = createUser("user1@test.com", "user1", "Learner One", Role.LEARNER);
        user2 = createUser("user2@test.com", "user2", "Learner Two", Role.LEARNER);
        moderator = createUser("mod@test.com", "mod", "Moderator User", Role.CULTURAL_EXPERT);

        user1Token = loginAndGetToken("user1@test.com", "password123");
        user2Token = loginAndGetToken("user2@test.com", "password123");
        moderatorToken = loginAndGetToken("mod@test.com", "password123");
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
    public void testPublicReadAndAuthenticatedWrite() throws Exception {
        // Unauthenticated read of posts -> 200 OK
        mockMvc.perform(get("/api/v1/community/posts"))
                .andExpect(status().isOk());

        // Unauthenticated write -> 401 UNAUTHORIZED
        CommunityPostCreateDto dto = new CommunityPostCreateDto();
        dto.setTitle("Unauthenticated Post");
        dto.setContent("Content here");
        dto.setCategory("DISCUSSION");

        mockMvc.perform(post("/api/v1/community/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        // Authenticated write -> 201 CREATED (Immediately VISIBLE)
        mockMvc.perform(post("/api/v1/community/posts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("VISIBLE"));
    }

    @Test
    public void testPlainTextSanitizationAndCharacterLimits() throws Exception {
        CommunityPostCreateDto dto = new CommunityPostCreateDto();
        dto.setTitle("Title with HTML <script>alert(1)</script>");
        dto.setContent("Content with HTML <b>bold text</b> and <a href='http://bad.com'>link</a>.");
        dto.setCategory("DISCUSSION");

        MvcResult res = mockMvc.perform(post("/api/v1/community/posts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String json = res.getResponse().getContentAsString();
        String title = objectMapper.readTree(json).get("data").get("title").asText();
        String content = objectMapper.readTree(json).get("data").get("content").asText();

        // Check HTML tags stripped
        assertFalse(title.contains("<script>"));
        assertFalse(content.contains("<b>"));
        assertFalse(content.contains("<a>"));

        // Test post character limit (> 2000 chars) -> 400 BAD_REQUEST
        dto.setContent("A".repeat(2001));
        mockMvc.perform(post("/api/v1/community/posts")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCommentTwoTierLimit() throws Exception {
        CommunityPost post = new CommunityPost();
        post.setTitle("Test Post");
        post.setContent("Content");
        post.setCategory("DISCUSSION");
        post.setStatus("VISIBLE");
        post.setAuthor(user1);
        post = postRepository.save(post);

        // Top level comment (Tier 1)
        CommunityCommentCreateDto comment1 = new CommunityCommentCreateDto();
        comment1.setContent("Top level comment");

        MvcResult res1 = mockMvc.perform(post("/api/v1/community/posts/" + post.getId() + "/comments")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment1)))
                .andExpect(status().isCreated())
                .andReturn();

        String comment1Id = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("id").asText();

        // Reply to top-level comment (Tier 2)
        CommunityCommentCreateDto comment2 = new CommunityCommentCreateDto();
        comment2.setContent("Reply comment");
        comment2.setParentCommentId(java.util.UUID.fromString(comment1Id));

        MvcResult res2 = mockMvc.perform(post("/api/v1/community/posts/" + post.getId() + "/comments")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment2)))
                .andExpect(status().isCreated())
                .andReturn();

        String comment2Id = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("id").asText();

        // Attempt to reply to reply comment (Tier 3) -> 400 BAD_REQUEST
        CommunityCommentCreateDto comment3 = new CommunityCommentCreateDto();
        comment3.setContent("Nested reply to reply");
        comment3.setParentCommentId(java.util.UUID.fromString(comment2Id));

        mockMvc.perform(post("/api/v1/community/posts/" + post.getId() + "/comments")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testReportingAndModerationWorkflow() throws Exception {
        CommunityPost post = new CommunityPost();
        post.setTitle("Reportable Post");
        post.setContent("Some inappropriate content");
        post.setCategory("DISCUSSION");
        post.setStatus("VISIBLE");
        post.setAuthor(user1);
        post = postRepository.save(post);

        // Self-reporting protection: user1 cannot report own post
        CommunityReportCreateDto reportDto = new CommunityReportCreateDto();
        reportDto.setTargetType("POST");
        reportDto.setTargetId(post.getId());
        reportDto.setReason("Spam or abusive content");

        mockMvc.perform(post("/api/v1/community/reports")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDto)))
                .andExpect(status().isBadRequest());

        // User 2 reports post -> 201 CREATED
        MvcResult reportRes = mockMvc.perform(post("/api/v1/community/reports")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();

        String reportId = objectMapper.readTree(reportRes.getResponse().getContentAsString()).get("data").get("id").asText();

        // Verify post remains VISIBLE (reporting does NOT automatically hide content)
        CommunityPost postAfterReport = postRepository.findById(post.getId()).orElseThrow();
        assertEquals("VISIBLE", postAfterReport.getStatus());

        // Duplicate report from user 2 -> 409 CONFLICT
        mockMvc.perform(post("/api/v1/community/reports")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportDto)))
                .andExpect(status().isConflict());

        // Moderator actions the report (hides content)
        mockMvc.perform(put("/api/v1/admin/community/reports/" + reportId + "/action")
                .header("Authorization", "Bearer " + moderatorToken)
                .param("action", "HIDE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIONED"));

        // Verify post is now HIDDEN
        CommunityPost postAfterAction = postRepository.findById(post.getId()).orElseThrow();
        assertEquals("HIDDEN", postAfterAction.getStatus());
    }
}

