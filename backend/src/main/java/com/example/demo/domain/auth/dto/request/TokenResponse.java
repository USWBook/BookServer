package com.example.demo.domain.auth.dto.request;

public record TokenResponse(
        String accessToken, String refreshToken
) {}
