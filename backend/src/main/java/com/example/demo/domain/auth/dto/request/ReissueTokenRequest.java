package com.example.demo.domain.auth.dto.request;

public record ReissueTokenRequest(
        String accessToken,
        String refreshToken
) {}
