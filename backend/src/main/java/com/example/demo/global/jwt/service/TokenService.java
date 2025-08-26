package com.example.demo.global.jwt.service;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenResponse generateTokens(String email, Role role) {
        String accessToken = jwtProvider.generateAccessToken(email, role);
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        redisTokenRepository.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void blacklistToken(String token) {
        long expiration = jwtProvider.getTokenRemainingTime(token);
        redisTokenRepository.blacklistToken(token, expiration);
    }

    @Transactional
    public void deleteRefreshToken(String email) {
        redisTokenRepository.deleteRefreshToken(email);
    }

    @Transactional
    public TokenResponse reissueTokens(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken) ||
                redisTokenRepository.isBlacklisted(refreshToken) ||
                !Objects.equals(jwtProvider.getCategory(refreshToken), "refresh") ||
                jwtProvider.isExpired(refreshToken)) {
            throw new JwtInvalidSignatureException();
        }

        String email = jwtProvider.extractEmail(refreshToken);

        if (!redisTokenRepository.existsRefreshToken(email)) {
            throw new JwtInvalidSignatureException();
        }


        redisTokenRepository.getRefreshToken(email)
                .filter(saved -> saved.equals(refreshToken))
                .orElseThrow(JwtInvalidSignatureException::new);

        User user = userRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        // 기존 refreshToken 레디스에서 삭제 처리
        deleteRefreshToken(email);
        //blacklistToken(refreshToken);

        // 새 토큰 발급
        return generateTokens(email, user.getRole());
    }

    @Transactional
    public boolean isBlacklisted(String token) {
        return redisTokenRepository.isBlacklisted(token);
    }

    @Transactional
    public String getEmailFromToken(String token) {
        return jwtProvider.extractEmail(token);
    }

    @Transactional
    public void banUser(String accessToken, String email) {
        // 1. 블랙리스트 등록
        long expiration = jwtProvider.getTokenRemainingTime(accessToken);
        redisTokenRepository.blacklistToken(accessToken, expiration);

        // 2. 리프레시 토큰 삭제
        redisTokenRepository.deleteRefreshToken(email);

        // 3. 사용자 상태 변경 (선택)
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user.ban();
    }

    public long getRefreshExpirationInMillis() {
        return jwtProvider.getRefreshTokenExpirationInMillis();
    }

    public void validateRefreshToken(String refreshToken, String email) {
        if (!Objects.equals(jwtProvider.extractEmail(refreshToken), email)) throw new JwtInvalidSignatureException();
    }
}

