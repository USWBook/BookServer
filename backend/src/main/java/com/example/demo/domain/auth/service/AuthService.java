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
        long expiration = jwtProvider.getAccessTokenRemainingTime(accessToken);

        redisTokenRepository.blacklistAccessToken(accessToken, expiration);
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
