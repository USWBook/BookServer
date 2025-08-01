package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
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
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @DisplayName("회원가입_성공")
    @Test
    void signUp_정상동작() {
        // given
        String email = "test@example.com";
        String password = "pw1234";
        String nickname = "testUser";
        String encodedPassword = "encoded";

        SignUpRequest request = new SignUpRequest(email, password, nickname);

        // 이메일 인증 여부 true로 설정
        when(redisTokenRepository.isVerifiedEmail(email)).thenReturn(true);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        authService.signUp(request);

        // then
        verify(redisTokenRepository).isVerifiedEmail(email);
        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("회원가입_실패_이메일_미인증")
    @Test
    void signUp_실패_인증안된이메일() {
        // given
        String email = "unverified@example.com";
        String password = "pw1234";
        String nickname = "noVerify";
        SignUpRequest request = new SignUpRequest(email, password, nickname);

        when(redisTokenRepository.isVerifiedEmail(email)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(EmailNotVerifiedException.class);

        verify(redisTokenRepository).isVerifiedEmail(email);
        verify(userRepository, never()).save(any());
    }


    @Test
    @DisplayName("로그인_성공")
    void login_정상동작() {
        // given
        String email = "test@example.com";
        String password = "pw1234";
        String encodedPassword = "encoded";

        User user = User.builder()
                .id(UUID.randomUUID())
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
    @DisplayName("회원가입되지 않은 이메일로 로그인 시 실패")
    void login_fail_userNotFound() {
        // given
        String email = "notfound@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserNotFoundException.class,
                () -> authService.login(new LoginRequest(email, password)));
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
                .id(UUID.randomUUID())
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

    @Test
    @DisplayName("저장된 리프레시 토큰과 불일치 시 재발급 실패")
    void reissue_fail_tokenMismatch() {
        // given
        String email = "test@example.com";
        String refreshToken = "input-token";
        String savedToken = "different-token";

        when(jwtProvider.isValid(refreshToken)).thenReturn(true);
        when(jwtProvider.extractEmail(refreshToken)).thenReturn(email);
        when(redisTokenRepository.getRefreshToken(email)).thenReturn(savedToken);

        // when & then
        assertThrows(InvalidTokenException.class,
                () -> authService.reissue(refreshToken));
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시 실패")
    void reissue_fail_invalidToken() {
        // given
        String refreshToken = "invalid-refresh-token";
        when(jwtProvider.isValid(refreshToken)).thenReturn(false);

        // when & then
        assertThrows(InvalidTokenException.class,
                () -> authService.reissue(refreshToken));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 로그인 실패")
    void login_wrongPassword() {
        // given
        String email = "test@example.com";
        String password = "wrong-password";
        String encodedPassword = "encoded-password";

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        // then
        assertThrows(InvalidPasswordException.class, () ->
                authService.login(new LoginRequest(email, password))
        );
    }

    @Test
    @DisplayName("밴된 사용자가 로그인 시도 시 실패")
    void login_fail_bannedUser() {
        // given
        String email = "banned@example.com";
        String password = "password123";
        String encodedPassword = "encoded-password";

        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .role(Role.USER)
                .status(UserStatus.BANNED) // ← 밴 상태
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        // then
        assertThrows(BannedUserException.class, () ->
                authService.login(new LoginRequest(email, password))
        );
    }

}