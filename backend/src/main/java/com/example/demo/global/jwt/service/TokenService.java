package com.example.demo.global.jwt.service;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.MemberNotFoundException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

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
        if (expiration > 0) {
            redisTokenRepository.blacklistToken(token, expiration);
        }
    }

    @Transactional
    public void deleteRefreshToken(String email) {
        redisTokenRepository.deleteRefreshToken(email);
    }

    @Transactional
    public TokenResponse reissueTokens(String refreshToken) {
        jwtProvider.validateToken(refreshToken);
        if (redisTokenRepository.isBlacklisted(refreshToken) ||
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
        blacklistToken(accessToken);

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


    // reissue시 이미 만료된 access토큰도 받아서 두 토큰 이메일 값도 비교해볼까 고민중
    // 이러면 리프레쉬 토큰만 탈취한 해커가 리프레시토큰 단독으론 못뚫지 않나 생각하면서도
    // 어차피 jwt라는게 암복호화가 쉬운거라 이메일 파싱해서 가짜access토큰 만들어버리면 그만이지 않나 싶음
    private void validateRefreshToken(String refreshToken, String email) {
        if (!Objects.equals(jwtProvider.extractEmail(refreshToken), email)) throw new JwtInvalidSignatureException();
    }
}