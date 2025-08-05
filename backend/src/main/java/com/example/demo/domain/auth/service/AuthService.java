package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signUp(SignUpRequest request) {

        // 회원가입되어있는지 검증
        if (userRepository.existsByEmail(request.email())) {
            throw new ExistEmailSignUpException();
        }

        // 이메일 인증 여부 확인
        if (!redisTokenRepository.isVerifiedEmail(request.email())) {
            throw new EmailNotVerifiedException();
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        // 인증 상태 삭제 (더 이상 필요 없으므로)
        redisTokenRepository.deleteVerifiedEmail(request.email());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            // 1. AuthenticationManager에게 인증 위임
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            // 2. 인증 성공 시 UserDetails(User 객체) 가져오기
            User user = (User) authentication.getPrincipal();

            if (!user.isEnabled()) {
                throw new DisabledException("계정이 활성화되어 있지 않습니다.");
            }

            // 3. 토큰 생성
            String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole());
            String refreshToken = jwtProvider.generateRefreshToken(user.getEmail(), user.getRole());

            // 4. Redis에 Refresh Token 저장
            redisTokenRepository.saveRefreshToken(
                    user.getEmail(),
                    refreshToken,
                    jwtProvider.getRefreshTokenExpirationInMillis()
            );

            return new TokenResponse(accessToken, refreshToken);

        } catch (AuthenticationException e) {
            // 5. 인증 실패 시 예외 처리
            // BannedUserException 등 특정 상태에 대한 분기는 UserDetails의 isAccountNonLocked() 등에서 처리됩니다.
            // DaoAuthenticationProvider가 적절한 예외(BadCredentialsException, LockedException 등)를 던져줍니다.
            log.warn("Login failed for email {}: {}", request.email(), e.getMessage());
            log.error(">>>> [AuthService] 인증 실패!", e);
            throw new AuthException(e.getMessage(),"400"); // 또는 더 구체적인 예외를 반환
        }
    }


    @Transactional
    public void logout(String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.extractEmail(token);

        // 1. 이미 블랙리스트에 있는지 확인
        if (redisTokenRepository.isBlacklisted(token)) {
            log.warn("[Logout] 이미 로그아웃 처리된 토큰입니다: {}", token);
            // 이미 처리되었으므로 별도 예외 없이 종료하거나,
            // 클라이언트에게 특정 응답을 주고 싶다면 예외를 던질 수도 있습니다.
            // 여기서는 조용히 종료합니다.
            return;
        }
        // 2. 남은 유효시간 계산 및 블랙리스트 등록
        long expiration = jwtProvider.getAccessTokenRemainingTime(token);
        redisTokenRepository.blacklistAccessToken(token, expiration);

        // 3. Refresh Token 삭제
        redisTokenRepository.deleteRefreshToken(email);
    }

    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // 1. 토큰 유효성 검사
        if (!jwtProvider.isValid(refreshToken)) {
            throw new JwtInvalidSignatureException();
        }

        // 2. 이메일 추출
        String email = jwtProvider.extractEmail(refreshToken);

        // 3. Redis에 저장된 refreshToken과 비교
        String savedToken = redisTokenRepository.getRefreshToken(email);
        if (!refreshToken.equals(savedToken)) {
            throw new JwtInvalidSignatureException();
        }

        // 4. 새로운 accessToken 발급
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String newAccessToken = jwtProvider.generateAccessToken(email, user.getRole());
        String newRefreshToken = jwtProvider.generateRefreshToken(email, user.getRole());

        // Redis에 새 refreshToken 저장, 기존 토큰 덮어쓰기
        redisTokenRepository.saveRefreshToken(email, newRefreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        return new TokenResponse(newAccessToken, newRefreshToken);
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

    @Transactional
    public void changePassword(String authHeader ,PasswordChangeRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void resetPassword(@Valid ResetPasswordRequest request) {

        // 이메일 인증 여부 확인
        if (!redisTokenRepository.isVerifiedEmail(request.email())) {
            throw new EmailNotVerifiedException();
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }
}
