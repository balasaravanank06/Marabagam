package com.marabagam.quiz.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.quiz.dto.CreateQuizDto;
import com.marabagam.quiz.dto.QuizDto;
import com.marabagam.quiz.service.QuizService;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/quizzes")
public class AdminQuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('ARTIST', 'INSTRUCTOR', 'CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(
            @RequestBody CreateQuizDto createDto,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        QuizDto quiz = quizService.createQuiz(createDto, user);
        return ResponseEntity.ok(ApiResponse.success("Draft quiz created successfully", quiz));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuizDto>> approveQuiz(
            @PathVariable UUID id,
            Authentication authentication) {
        User user = getUserFromAuth(authentication);
        QuizDto quiz = quizService.approveQuiz(id, user);
        return ResponseEntity.ok(ApiResponse.success("Quiz approved successfully", quiz));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('CULTURAL_EXPERT', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuizDto>> publishQuiz(@PathVariable UUID id) {
        QuizDto quiz = quizService.publishQuiz(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz published successfully", quiz));
    }

    private User getUserFromAuth(Authentication authentication) {
        String identifier = authentication.getName();
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResourceNotFoundException("User not found for identifier: " + identifier));
    }
}
