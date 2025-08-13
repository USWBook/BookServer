package com.example.demo.global.jwt;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;

    public TokenResponse createAndStoreTokens(String email, Role role) {
        String accessToken = jwtProvider.generateAccessToken(email, role);
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        redisTokenRepository.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        return new TokenResponse(accessToken, refreshToken);
    }
}

