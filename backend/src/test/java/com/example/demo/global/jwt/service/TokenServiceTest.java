package com.example.demo.global.jwt.service;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private String email;
    private UUID userId;

    @BeforeEach
    void setUp() {
        email = "test@suwon.ac.kr";
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email(email)
                .role(Role.USER)
                .build();
    }


    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueTokens_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        // 1. 초기 유효성 검사 통과 설정
        doNothing().when(jwtProvider).validateToken(refreshToken);
        given(redisTokenRepository.isBlacklisted(refreshToken)).willReturn(false);
        given(jwtProvider.getCategory(refreshToken)).willReturn("refresh");
        given(jwtProvider.isExpired(refreshToken)).willReturn(false);

        // 2. 이메일 추출 및 Redis 검증 통과 설정
        given(jwtProvider.extractEmail(refreshToken)).willReturn(email);
        given(redisTokenRepository.existsRefreshToken(email)).willReturn(true);
        given(redisTokenRepository.getRefreshToken(email)).willReturn(Optional.of(refreshToken));

        // 3. 사용자 조회 성공 설정
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // 4. 새 토큰 생성 성공 설정
        given(jwtProvider.generateAccessToken(user.getId(), email, user.getRole())).willReturn(newAccessToken);
        given(jwtProvider.generateRefreshToken(user.getId(), email, user.getRole())).willReturn(newRefreshToken);

        // when
        TokenResponse tokenResponse = tokenService.reissueTokens(refreshToken);

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo(newAccessToken);

        verify(redisTokenRepository).deleteRefreshToken(email);
        verify(redisTokenRepository).saveRefreshToken(email, newRefreshToken, jwtProvider.getRefreshTokenExpirationInMillis());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis 토큰 불일치")
    void reissueTokens_Fail_TokenMismatchInRedis() {
        // given
        String requestToken = "request-refresh-token";
        String savedTokenInRedis = "saved-different-token";

        // 초기 유효성 검사 통과
        doNothing().when(jwtProvider).validateToken(requestToken);
        given(redisTokenRepository.isBlacklisted(requestToken)).willReturn(false);
        given(jwtProvider.getCategory(requestToken)).willReturn("refresh");
        given(jwtProvider.isExpired(requestToken)).willReturn(false);
        given(jwtProvider.extractEmail(requestToken)).willReturn(email);
        given(redisTokenRepository.existsRefreshToken(email)).willReturn(true);

        // 핵심: Redis에 저장된 토큰이 요청 토큰과 다르다고 가정
        given(redisTokenRepository.getRefreshToken(email)).willReturn(Optional.of(savedTokenInRedis));

        // when & then
        assertThrows(JwtInvalidSignatureException.class, () -> tokenService.reissueTokens(requestToken));

        verify(userRepository, never()).findByEmail(any());
        verify(redisTokenRepository, never()).deleteRefreshToken(any());
    }

    @Test
    @DisplayName("토큰 발급 실패 - Redis 저장 중 예외 발생")
    void generateTokens_Fail_RedisError() {
        // given
        given(jwtProvider.generateAccessToken(user.getId(), email, user.getRole())).willReturn("any-access-token");
        given(jwtProvider.generateRefreshToken(user.getId(), email, user.getRole())).willReturn("any-refresh-token");

        // 핵심: redisTokenRepository.saveRefreshToken이 호출될 때 RuntimeException을 던지도록 설정
        doThrow(new RuntimeException("Redis connection failed"))
                .when(redisTokenRepository).saveRefreshToken(anyString(), anyString(), anyLong());

        // when & then
        assertThrows(RuntimeException.class, () -> tokenService.generateTokens(user.getId(), email, user.getRole()));
    }

    @Test
    @DisplayName(" 토큰 발급 및 저장 성공")
    void generateTokens_Success() {
        // given
        String expectedAccessToken = "generated-access-token";
        String expectedRefreshToken = "generated-refresh-token";

        given(jwtProvider.generateAccessToken(user.getId(), email, user.getRole())).willReturn(expectedAccessToken);
        given(jwtProvider.generateRefreshToken(user.getId(), email, user.getRole())).willReturn(expectedRefreshToken);

        // when
        TokenResponse tokenResponse = tokenService.generateTokens(user.getId(), email, user.getRole());

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo(expectedAccessToken);
        assertThat(tokenResponse.refreshToken()).isEqualTo(expectedRefreshToken);

        verify(redisTokenRepository).saveRefreshToken(
                email,
                expectedRefreshToken,
                jwtProvider.getRefreshTokenExpirationInMillis()
        );
    }
}