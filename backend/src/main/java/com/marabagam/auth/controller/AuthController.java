package com.marabagam.auth.controller;

import com.marabagam.auth.dto.*;
import com.marabagam.auth.service.AuthService;
import com.marabagam.common.response.ApiResponse;
import com.marabagam.user.domain.User;
import com.marabagam.user.dto.UserResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto registeredUser = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        AuthResponseDto authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken(), 604800);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            @RequestBody(required = false) TokenRefreshRequestDto request,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            HttpServletResponse response) {
        
        String tokenToRefresh = (request != null && request.getRefreshToken() != null)
                ? request.getRefreshToken()
                : cookieRefreshToken;

        if (tokenToRefresh == null || tokenToRefresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Refresh token must be provided via cookie or request body"));
        }

        TokenRefreshRequestDto refreshDto = new TokenRefreshRequestDto();
        refreshDto.setRefreshToken(tokenToRefresh);

        AuthResponseDto authResponse = authService.refreshToken(refreshDto);
        setRefreshTokenCookie(response, authResponse.getRefreshToken(), 604800);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody(required = false) TokenRefreshRequestDto request,
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @AuthenticationPrincipal User currentUser,
            HttpServletResponse response) {

        String tokenToRevoke = (request != null && request.getRefreshToken() != null)
                ? request.getRefreshToken()
                : cookieRefreshToken;

        authService.logout(tokenToRevoke, currentUser);
        setRefreshTokenCookie(response, "", 0);

        return ResponseEntity.ok(ApiResponse.success("Logout successful", "Session and tokens revoked"));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAgeSeconds) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in HTTPS production
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }
}
