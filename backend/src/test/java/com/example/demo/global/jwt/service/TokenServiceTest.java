package com.example.demo.global.jwt.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private Authentication authentication;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissueTokens_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        String email = "test@suwon.ac.kr";
        User user = User.builder().email(email).role(Role.USER).build();
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        // 1. 초기 유효성 검사 통과 설정
        given(jwtProvider.isValid(refreshToken)).willReturn(true);
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
        given(jwtProvider.generateAccessToken(email, user.getRole())).willReturn(newAccessToken);
        given(jwtProvider.generateRefreshToken(email, user.getRole())).willReturn(newRefreshToken);

        // when
        TokenResponse tokenResponse = tokenService.reissueTokens(refreshToken);

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo(newAccessToken);

        // 기존 토큰 삭제 및 새 토큰 저장 로직 호출 검증
        verify(redisTokenRepository).deleteRefreshToken(email);
        verify(redisTokenRepository).saveRefreshToken(email, newRefreshToken, jwtProvider.getRefreshTokenExpirationInMillis());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - Redis 토큰 불일치")
    void reissueTokens_Fail_TokenMismatchInRedis() {
        // given
        String requestToken = "request-refresh-token";
        String savedTokenInRedis = "saved-different-token";
        String email = "test@suwon.ac.kr";

        // 초기 유효성 검사 통과
        given(jwtProvider.isValid(requestToken)).willReturn(true);
        given(redisTokenRepository.isBlacklisted(requestToken)).willReturn(false);
        given(jwtProvider.getCategory(requestToken)).willReturn("refresh");
        given(jwtProvider.isExpired(requestToken)).willReturn(false);
        given(jwtProvider.extractEmail(requestToken)).willReturn(email);
        given(redisTokenRepository.existsRefreshToken(email)).willReturn(true);

        //  핵심: Redis에 저장된 토큰이 요청 토큰과 다르다고 가정
        given(redisTokenRepository.getRefreshToken(email)).willReturn(Optional.of(savedTokenInRedis));

        // when & then
        assertThrows(JwtInvalidSignatureException.class, () -> {
            tokenService.reissueTokens(requestToken);
        });

        // 예외 발생 후 후속 로직이 실행되지 않았는지 검증
        verify(userRepository, never()).findByEmail(any());
        verify(redisTokenRepository, never()).deleteRefreshToken(any());
    }

    @Test
    @DisplayName("토큰 발급 실패 - Redis 저장 중 예외 발생")
    void generateTokens_Fail_RedisError() {
        // given
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;

        // jwtProvider는 정상적으로 토큰을 생성한다고 가정
        given(jwtProvider.generateAccessToken(email, role)).willReturn("any-access-token");
        given(jwtProvider.generateRefreshToken(email, role)).willReturn("any-refresh-token");

        //  핵심: redisTokenRepository.saveRefreshToken이 호출될 때 RuntimeException을 던지도록 설정
        doThrow(new RuntimeException("Redis connection failed"))
                .when(redisTokenRepository).saveRefreshToken(anyString(), anyString(), anyLong());

        // when & then
        // generateTokens를 호출했을 때, 내부의 RuntimeException이 그대로 전파되는지 검증
        assertThrows(RuntimeException.class, () -> {
            tokenService.generateTokens(email, role);
        });
    }

    @Test
    @DisplayName("이메일과 역할(Role) 기반으로 토큰 발급 및 저장 성공")
    void generateTokens_Success() {
        // given
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;
        String expectedAccessToken = "generated-access-token";
        String expectedRefreshToken = "generated-refresh-token";

        //  JwtProvider가 토큰을 잘 생성할 것이라고 가정
        given(jwtProvider.generateAccessToken(email, role)).willReturn(expectedAccessToken);
        given(jwtProvider.generateRefreshToken(email, role)).willReturn(expectedRefreshToken);

        // when
        //  핵심 로직이 담긴 메서드를 직접 호출
        TokenResponse tokenResponse = tokenService.generateTokens(email, role);

        // then
        // 1. 반환된 토큰이 예상과 일치하는지 검증
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isEqualTo(expectedAccessToken);
        assertThat(tokenResponse.refreshToken()).isEqualTo(expectedRefreshToken);

        // 2. Refresh Token이 Redis에 저장되었는지 검증
        verify(redisTokenRepository).saveRefreshToken(
                email,
                expectedRefreshToken,
                jwtProvider.getRefreshTokenExpirationInMillis()
        );
    }

}