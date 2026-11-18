package com.marabagam.learning.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.learning.dto.RecommendationFeedDto;
import com.marabagam.learning.service.RecommendationService;
import com.marabagam.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RecommendationFeedDto>> getRecommendations(@AuthenticationPrincipal User currentUser) {
        RecommendationFeedDto feed = recommendationService.getPersonalizedRecommendations(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved successfully", feed));
    }
}
