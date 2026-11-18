package com.marabagam.auth.dto;

import com.marabagam.user.dto.UserResponseDto;

public class AuthResponseDto {

    private String accessToken;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private String refreshToken;
    private UserResponseDto user;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String accessToken, long expiresInMs, String refreshToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.expiresInMs = expiresInMs;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }
}
