package com.marabagam.quiz.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.quiz.dto.AttemptResultDto;
import com.marabagam.quiz.dto.QuizDto;
import com.marabagam.quiz.dto.QuizSubmissionDto;
import com.marabagam.quiz.service.QuizService;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizForLearner(@PathVariable UUID id) {
        QuizDto quiz = quizService.getQuizForLearner(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", quiz));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttemptResultDto>> startAttempt(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AttemptResultDto result = quizService.startQuizAttempt(id, user);
        return ResponseEntity.ok(ApiResponse.success("Quiz attempt started", result));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttemptResultDto>> submitAttempt(
            @PathVariable UUID attemptId,
            @RequestBody QuizSubmissionDto submission,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AttemptResultDto result = quizService.submitQuizAttempt(attemptId, submission, user);
        return ResponseEntity.ok(ApiResponse.success("Quiz attempt submitted successfully", result));
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("hasAnyRole('LEARNER', 'ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<AttemptResultDto>> getAttemptResult(
            @PathVariable UUID attemptId,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        AttemptResultDto result = quizService.getAttemptResult(attemptId, user);
        return ResponseEntity.ok(ApiResponse.success("Attempt result retrieved successfully", result));
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
