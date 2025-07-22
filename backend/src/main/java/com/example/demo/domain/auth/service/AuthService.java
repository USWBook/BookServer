package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.request.TokenResponse;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.mail.service.MailService;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
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
    private final MailService mailService;

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
                .status(UserStatus.INACTIVE)
                .build();

        userRepository.save(user);

        mailService.sendVerificationCode(request.email());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        if (user.isBanned()) {
            throw new BannedUserException();
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        // 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(),user.getRole());

        // Redis에 Refresh Token 저장 (email 기준으로)
        /*
            JWT에 만료시간이 있는데 굳이 expirationMillis를 따로 Redis에 넣는 이유
         Redis TTL(Time-To-Live) 설정 때문
         Redis에 저장된 리프레시 토큰은 수동으로 삭제하지 않으면 계속 남아 있기에
         TTL을 설정하지 않으면 Redis에 영구적으로 남음.
         내부적으로는 RedisTemplate.opsForValue().set(key, value, expirationMillis, TimeUnit.MILLISECONDS)
         이게 Redis에 자동으로 만료되도록 설정해주는 핵심
         리프레시 토큰 만료 시간만큼 Redis에 자동으로 남아 있도록 설정하는 역할.
         토큰과 서버 저장소의 만료 시점을 맞추기 위해 필요
         */
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
        /*
            액세스 토큰 블랙리스트에 남은 유효시간을 왜 넘기는 이유
        Redis에서 블랙리스트 자동 삭제를 위해서.
        액세스 토큰은 만료 시간이 지나면 쓸모가 없음.
        따라서 블랙리스트에도 너무 오래 남아 있을 필요가 없음.
        Redis에 넣되, 남은 유효시간만큼만 유지하고 자동 삭제되게 하려는 목적.
        내부적으로는 Redis에 set(token, true, expiration) 형태로 저장되어
        해당 시간이 지나면 자동 삭제돼서 메모리를 아낄 수 있음.
         */
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

    public void banUser(String accessToken, String email) {
        // 1. 블랙리스트 등록
        long expiration = jwtProvider.getAccessTokenRemainingTime(accessToken);
        redisTokenRepository.blacklistAccessToken(accessToken, expiration);

        // 2. 리프레시 토큰 삭제
        redisTokenRepository.deleteRefreshToken(email);

        // 3. 사용자 상태 변경 (선택)
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user.ban();
    }

}
