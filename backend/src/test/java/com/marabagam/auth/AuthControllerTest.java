package com.marabagam.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marabagam.auth.dto.LoginRequestDto;
import com.marabagam.auth.dto.RegisterRequestDto;
import com.marabagam.auth.dto.TokenRefreshRequestDto;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.dto.ChangePasswordRequestDto;
import com.marabagam.user.dto.UpdateProfileRequestDto;
import com.marabagam.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationSuccess() throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("artist@marabagam.org");
        registerDto.setUsername("bharatanatyam_artist");
        registerDto.setPassword("SecurePass123!");
        registerDto.setDisplayName("Ananya Raman");
        registerDto.setPreferredLanguage("ta");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("artist@marabagam.org"))
                .andExpect(jsonPath("$.data.role").value("LEARNER")); // Default safe role
    }

    @Test
    public void testDuplicateEmailRegistrationFails() throws Exception {
        RegisterRequestDto dto = createTestRegisterDto("test@marabagam.org", "user1");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        RegisterRequestDto duplicateDto = createTestRegisterDto("test@marabagam.org", "user2");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testLoginSuccessAndFetchProfile() throws Exception {
        RegisterRequestDto registerDto = createTestRegisterDto("learner@marabagam.org", "learner1");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsernameOrEmail("learner@marabagam.org");
        loginDto.setPassword("SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseJson).get("data").get("accessToken").asText();

        // Protected Endpoint Access Verification
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("learner@marabagam.org"))
                .andExpect(jsonPath("$.data.role").value("LEARNER"));
    }

    @Test
    public void testInvalidLoginFails() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsernameOrEmail("nonexistent@marabagam.org");
        loginDto.setPassword("WrongPassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testUnauthenticatedAccessFails() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateProfileAndChangePassword() throws Exception {
        RegisterRequestDto registerDto = createTestRegisterDto("profile@marabagam.org", "profileUser");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setUsernameOrEmail("profileUser");
        loginDto.setPassword("SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Profile Update
        UpdateProfileRequestDto updateDto = new UpdateProfileRequestDto();
        updateDto.setDisplayName("Updated Tamil Scholar");
        updateDto.setPreferredLanguage("ta");

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("Updated Tamil Scholar"))
                .andExpect(jsonPath("$.data.preferredLanguage").value("ta"));

        // Change Password
        ChangePasswordRequestDto changePasswordDto = new ChangePasswordRequestDto();
        changePasswordDto.setCurrentPassword("SecurePass123!");
        changePasswordDto.setNewPassword("NewSecurePass456!");

        mockMvc.perform(post("/api/v1/users/me/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isOk());
    }

    private RegisterRequestDto createTestRegisterDto(String email, String username) {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setEmail(email);
        dto.setUsername(username);
        dto.setPassword("SecurePass123!");
        dto.setDisplayName("Test User");
        dto.setPreferredLanguage("en");
        return dto;
    }
}
