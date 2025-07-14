package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.request.TokenResponse;
import com.example.demo.domain.auth.exception.ExistEmailSignUpException;
import com.example.demo.domain.auth.exception.InvalidPasswordException;
import com.example.demo.domain.auth.exception.InvalidTokenException;
import com.example.demo.domain.auth.exception.UserNotFoundException;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ExistEmailSignUpException();
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(),user.getRole());

        // Redis에 Refresh Token 저장 (email 기준으로)
        redisTokenRepository.saveRefreshToken(
                user.getEmail(),
                refreshToken,
                jwtProvider.getRefreshTokenExpirationInMillis()
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(String accessToken, String email) {

        // 1. 이미 블랙리스트에 있는지 확인
        if (redisTokenRepository.isBlacklisted(accessToken)) {
            log.warn("[Logout] 이미 로그아웃 처리된 토큰입니다: {}", accessToken);
            // 이미 처리되었으므로 별도 예외 없이 종료하거나,
            // 클라이언트에게 특정 응답을 주고 싶다면 예외를 던질 수도 있습니다.
            // 여기서는 조용히 종료합니다.
            return;
        }
        // 2. 남은 유효시간 계산 및 블랙리스트 등록
        long expiration = jwtProvider.getAccessTokenRemainingTime(accessToken);
        redisTokenRepository.blacklistAccessToken(accessToken, expiration);

        // 3. Refresh Token 삭제
        redisTokenRepository.deleteRefreshToken(email);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // 1. 토큰 유효성 검사
        if (!jwtProvider.isValid(refreshToken)) {
            throw new InvalidTokenException();
        }

        // 2. 이메일 추출
        String email = jwtProvider.extractEmail(refreshToken);

        // 3. Redis에 저장된 refreshToken과 비교
        String savedToken = redisTokenRepository.getRefreshToken(email);
        if (!refreshToken.equals(savedToken)) {
            throw new InvalidTokenException();
        }

        // 4. 새로운 accessToken 발급
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = jwtProvider.generateAccessToken(email, user.getRole());

        return new TokenResponse(newAccessToken, refreshToken);
    }

}
