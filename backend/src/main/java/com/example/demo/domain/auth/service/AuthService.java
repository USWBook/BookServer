package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

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

//    @Transactional
//    public TokenResponse login(LoginRequest request) {
//        try {
//            // 1. AuthenticationManager에게 인증 위임
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
//            );
//
//            // 2. 인증 성공 시 UserDetails(User 객체) 가져오기
//            //User user = (User) authentication.getPrincipal();
//            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//            return tokenService.generateTokens(userPrincipal.getUsername(), userPrincipal.getRole());
//
//
//        } catch (AuthenticationException e) {
//            // 5. 인증 실패 시 예외 처리
//            // BannedUserException 등 특정 상태에 대한 분기는 UserDetails의 isAccountNonLocked() 등에서 처리됩니다.
//            // DaoAuthenticationProvider가 적절한 예외(BadCredentialsException, LockedException 등)를 던져줍니다.
//            log.warn("Login failed for email {}: {}", request.email(), e.getMessage());
//            log.error(">>>> [AuthService] 인증 실패!", e);
//            throw new AuthException(e.getMessage(),"400");
//        }
//    }


    @Transactional
    public void logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                       @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        // AccessToken 블랙리스트 처리
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring("Bearer ".length());
            tokenService.blacklistToken(accessToken);
        }

        // RefreshToken 블랙리스트 처리
        if (refreshToken != null && !refreshToken.isEmpty()) {
            tokenService.blacklistToken(refreshToken);

            String email = tokenService.getEmailFromToken(refreshToken);
            tokenService.deleteRefreshToken(email);
        }

    }

    @Transactional
    public TokenResponse reissue(@CookieValue("refreshToken") String refreshToken) {
        return tokenService.reissueTokens(refreshToken);
    }


    @Transactional
    public void changePassword(String authHeader ,PasswordChangeRequest request) {

        String token = authHeader.replace("Bearer ", "");
        String email = tokenService.getEmailFromToken(token);

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
