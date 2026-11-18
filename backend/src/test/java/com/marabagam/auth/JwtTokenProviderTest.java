package com.marabagam.auth;

import com.marabagam.auth.security.JwtTokenProvider;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    public void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 900000L);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("learner@marabagam.org");
        testUser.setUsername("tamil_learner");
        testUser.setRole(Role.LEARNER);
    }

    @Test
    public void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        UUID extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(testUser.getId(), extractedUserId);
    }

    @Test
    public void testInvalidTokenFailsValidation() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }
}
