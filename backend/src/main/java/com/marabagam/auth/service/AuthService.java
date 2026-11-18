package com.marabagam.auth.service;

import com.marabagam.auth.domain.RefreshToken;
import com.marabagam.auth.dto.*;
import com.marabagam.auth.repository.RefreshTokenRepository;
import com.marabagam.auth.security.JwtTokenProvider;
import com.marabagam.common.exception.AuthException;
import com.marabagam.user.domain.AccountStatus;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.dto.UserResponseDto;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("EMAIL_EXISTS", "A user with this email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("USERNAME_EXISTS", "A user with this username already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName().trim());
        user.setPreferredLanguage(request.getPreferredLanguage() != null ? request.getPreferredLanguage() : "en");
        
        // Strict Security Enforcement: Public registration defaults to LEARNER
        user.setRole(Role.LEARNER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        return UserResponseDto.fromEntity(savedUser);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        String identifier = request.getUsernameOrEmail().trim();
        User user = userRepository.findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() -> new AuthException("INVALID_CREDENTIALS", "Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("INVALID_CREDENTIALS", "Invalid username/email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AuthException("ACCOUNT_DISABLED", "Your account is not active. Please contact support.");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        saveRefreshToken(user, rawRefreshToken);

        log.info("User logged in successfully: {}", user.getId());
        return new AuthResponseDto(
                accessToken,
                jwtTokenProvider.getExpirationMs(),
                rawRefreshToken,
                UserResponseDto.fromEntity(user)
        );
    }

    @Transactional
    public AuthResponseDto refreshToken(TokenRefreshRequestDto request) {
        String tokenHash = hashToken(request.getRefreshToken());
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthException("INVALID_TOKEN", "Refresh token is invalid or does not exist"));

        if (token.isRevoked()) {
            // Revocation security: If a revoked token is reused, revoke all user tokens (Token Reuse Detection)
            refreshTokenRepository.revokeAllByUser(token.getUser());
            throw new AuthException("REFRESH_TOKEN_REVOKED", "Refresh token has been revoked");
        }

        if (token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new AuthException("TOKEN_EXPIRED", "Refresh token has expired");
        }

        User user = token.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AuthException("ACCOUNT_DISABLED", "Account is disabled");
        }

        // Token Rotation: Revoke old token and issue new token pair
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRawRefreshToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        saveRefreshToken(user, newRawRefreshToken);

        return new AuthResponseDto(
                newAccessToken,
                jwtTokenProvider.getExpirationMs(),
                newRawRefreshToken,
                UserResponseDto.fromEntity(user)
        );
    }

    @Transactional
    public void logout(String rawRefreshToken, User currentUser) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
        if (currentUser != null) {
            refreshTokenRepository.revokeAllByUser(currentUser);
        }
        log.info("Logout completed for user");
    }

    private void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        token.setRevoked(false);
        refreshTokenRepository.save(token);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
