package com.marabagam.user.service;

import com.marabagam.auth.repository.RefreshTokenRepository;
import com.marabagam.common.exception.AuthException;
import com.marabagam.user.domain.User;
import com.marabagam.user.dto.ChangePasswordRequestDto;
import com.marabagam.user.dto.UpdateProfileRequestDto;
import com.marabagam.user.dto.UserResponseDto;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDto getUserProfile(User currentUser) {
        return UserResponseDto.fromEntity(currentUser);
    }

    @Transactional
    public UserResponseDto updateProfile(User currentUser, UpdateProfileRequestDto request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));

        user.setDisplayName(request.getDisplayName().trim());
        user.setPreferredLanguage(request.getPreferredLanguage().trim());

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", updatedUser.getId());

        return UserResponseDto.fromEntity(updatedUser);
    }

    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequestDto request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException("INVALID_CREDENTIALS", "Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Security Control: Revoke all active refresh tokens after password change
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Password changed and active sessions revoked for user: {}", user.getId());
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "User not found with email: " + email));
    }

    public User getUserByEmail(String email) {
        return getByEmail(email);
    }
}

