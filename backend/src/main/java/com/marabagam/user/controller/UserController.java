package com.marabagam.user.controller;

import com.marabagam.common.response.ApiResponse;
import com.marabagam.user.domain.User;
import com.marabagam.user.dto.ChangePasswordRequestDto;
import com.marabagam.user.dto.UpdateProfileRequestDto;
import com.marabagam.user.dto.UserResponseDto;
import com.marabagam.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        UserResponseDto profile = userService.getUserProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Current user profile retrieved", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        UserResponseDto updatedProfile = userService.updateProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedProfile));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        userService.changePassword(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", "Active sessions revoked"));
    }
}
