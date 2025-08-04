package com.example.demo.domain.auth.dto.response;

public record TokenResponse(
        String accessToken, String refreshToken
)   {
    public TokenResponse withoutRefreshToken() {
    return new TokenResponse(this.accessToken, null);
    }
}
