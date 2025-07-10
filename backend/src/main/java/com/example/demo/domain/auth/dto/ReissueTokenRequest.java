package com.example.demo.domain.auth.dto;

public record ReissueTokenRequest(
        String accessToken,
        String refreshToken
) {}
