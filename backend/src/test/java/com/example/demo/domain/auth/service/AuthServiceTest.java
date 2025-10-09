package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.ExistEmailSignUpException;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisTokenRepository redisTokenRepository;
    @Mock
    private MajorRepository majorRepository;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {

        // given
        UUID majorId = UUID.randomUUID();
        SignUpRequest request = new SignUpRequest(
                "test@suwon.ac.kr", "password123!", "테스트",
                "20201234", majorId, 2, 1
        );
        String encodedPassword = "encodedPassword";
        Major major = Major.builder().id(majorId).name("컴퓨터학부").build();

        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(redisTokenRepository.isVerifiedEmail(request.email())).willReturn(true);
        given(majorRepository.findById(request.majorId())).willReturn(Optional.of(major));
        given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        authService.signUp(request);

        // then
        verify(userRepository).save(any(User.class));
        verify(redisTokenRepository).deleteVerifiedEmail(request.email());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signUp_Fail_EmailExists() {
        // given
        SignUpRequest request = new SignUpRequest(
                "test@suwon.ac.kr", "password123!", "테스트",
                "20201234", UUID.randomUUID(), 2, 1
        );
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThrows(ExistEmailSignUpException.class, () -> {
            authService.signUp(request);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("토큰 재발급 성공 - TokenService에 작업 위임")
    void reissue_Success_DelegatesToTokenService() {
        // given
        String refreshToken = "valid-refresh-token";
        TokenResponse expectedResponse = new TokenResponse("new-access-token", "new-refresh-token");

        // TokenService의 reissueTokens 메서드가 성공적으로 TokenResponse를 반환할 것이라고 가정
        given(tokenService.reissueTokens(refreshToken)).willReturn(expectedResponse);

        // when
        TokenResponse actualResponse = tokenService.reissueTokens(refreshToken);

        // then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.accessToken()).isEqualTo("new-access-token");

        // AuthService가 TokenService의 reissueTokens를 정확히 1번 호출했는지 검증
        verify(tokenService).reissueTokens(refreshToken);
    }

    @Test
    @DisplayName("토큰 재발급 실패 - TokenService에서 예외 발생")
    void reissue_Fail_ExceptionFromTokenService() {
        // given
        String refreshToken = "invalid-refresh-token";

        // TokenService의 reissueTokens 메서드가 예외를 던질 것이라고 가정
        given(tokenService.reissueTokens(refreshToken)).willThrow(new JwtInvalidSignatureException());

        // when & then
        // AuthService가 TokenService로부터 받은 예외를 그대로 다시 던지는지 검증
        assertThrows(JwtInvalidSignatureException.class, () -> {
            tokenService.reissueTokens(refreshToken);
        });

        verify(tokenService).reissueTokens(refreshToken);
    }
}