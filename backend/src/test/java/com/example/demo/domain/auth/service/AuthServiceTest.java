package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.LoginRequest;
import com.example.demo.domain.auth.dto.TokenResponse;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_정상동작() {
        // given
        String email = "test@example.com";
        String password = "pw1234";
        String encodedPassword = "encoded";

        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtProvider.generateAccessToken(email, Role.USER)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(email,Role.USER)).thenReturn("refresh-token");

        // when
        TokenResponse response = authService.login(new LoginRequest(email, password));

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(redisTokenRepository).saveRefreshToken(eq(email), eq("refresh-token"), anyLong());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 accessToken 재발급 성공")
    void reissue_success() {

        login_정상동작();

        // given
        String email = "test@example.com";
        String password = "pw1234";
        String encodedPassword = "encoded";
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";


        User user = User.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();


        when(jwtProvider.isValid(refreshToken)).thenReturn(true);
        when(jwtProvider.extractEmail(refreshToken)).thenReturn(email);
        when(redisTokenRepository.getRefreshToken(email)).thenReturn(refreshToken);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(email, user.getRole())).thenReturn(newAccessToken);

        // when
        TokenResponse response = authService.reissue(refreshToken);

        // then
        assertEquals(newAccessToken, response.accessToken());
        assertEquals(refreshToken, response.refreshToken());
    }

}