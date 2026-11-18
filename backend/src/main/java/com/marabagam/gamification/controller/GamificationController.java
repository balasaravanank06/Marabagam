package com.marabagam.gamification.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.gamification.dto.GamificationProfileDto;
import com.marabagam.gamification.dto.LeaderboardEntryDto;
import com.marabagam.gamification.service.GamificationService;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gamification")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<GamificationProfileDto>> getProfile(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        GamificationProfileDto profile = gamificationService.getUserProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Gamification profile retrieved successfully", profile));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(defaultValue = "global") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<LeaderboardEntryDto> leaderboard = "weekly".equalsIgnoreCase(type)
                ? gamificationService.getWeeklyLeaderboard(page, size)
                : gamificationService.getGlobalLeaderboard(page, size);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard retrieved successfully", leaderboard));
    }

    @PutMapping("/privacy")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> toggleLeaderboardPrivacy(
            @RequestParam boolean visible,
            Authentication authentication
    ) {
        User user = getUserFromAuth(authentication);
        user.setLeaderboardVisible(visible);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard visibility updated successfully", visible));
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        String identifier = authentication != null ? authentication.getName() : null;
        if (identifier == null) {
            throw new ResourceNotFoundException("Unauthenticated user");
        }
        try {
            UUID userId = UUID.fromString(identifier);
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) return userOpt.get();
        } catch (IllegalArgumentException ignored) {}

        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found for identifier: " + identifier));
    }
}
