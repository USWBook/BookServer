package com.example.demo.domain.auth.dto.response;

public record TokenResponse(
        String accessToken, String refreshToken
)   {
    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
