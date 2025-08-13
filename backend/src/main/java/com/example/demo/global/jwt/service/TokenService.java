package com.example.demo.global.jwt.service;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final UserRepository userRepository;

    public TokenResponse generateTokens(String email, Role role) {
        String accessToken = jwtProvider.generateAccessToken(email, role);
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        redisTokenRepository.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        return new TokenResponse(accessToken, refreshToken);
    }

    public void blacklistAccessToken(String token) {
        long expiration = jwtProvider.getTokenRemainingTime(token);
        redisTokenRepository.blacklistToken(token, expiration);
    }

    public void deleteRefreshToken(String email) {
        redisTokenRepository.deleteRefreshToken(email);
    }

    public TokenResponse reissueTokens(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken) || redisTokenRepository.isBlacklisted(refreshToken)) {
            throw new JwtInvalidSignatureException();
        }

        String email = jwtProvider.extractEmail(refreshToken);
        String savedToken = redisTokenRepository.getRefreshToken(email);
        if (!refreshToken.equals(savedToken)) {
            throw new JwtInvalidSignatureException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 기존 refreshToken 블랙리스트 처리
        long remainingRefreshMillis = jwtProvider.getTokenRemainingTime(refreshToken);
        redisTokenRepository.blacklistToken(savedToken, remainingRefreshMillis);

        // 새 토큰 발급
        return generateTokens(email, user.getRole());
    }

    public boolean isBlacklisted(String token) {
        return redisTokenRepository.isBlacklisted(token);
    }
}

