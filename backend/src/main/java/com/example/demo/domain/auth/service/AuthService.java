package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.entity.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.exception.AuthException;
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

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;
    private final TokenService tokenService;
    private final MajorRepository majorRepository;

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

        Major major = majorRepository.findById(request.majorId())
                .orElseThrow(MajorNotFoundException::new);

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .studentId(request.studentId())
                .major(major)
                .grade(request.grade())
                .semester(request.semester())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);

        // 인증 상태 삭제 (더 이상 필요 없으므로)
        redisTokenRepository.deleteVerifiedEmail(request.email());
    }


    @Transactional
    public TokenResponse reissue(@CookieValue("refreshToken") String refreshToken) {
        return tokenService.reissueTokens(refreshToken);
    }


    @Transactional
    public void changePassword(String email ,PasswordChangeRequest request) {

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

    /**
     * 로그인 성공 후 처리: 필요 시 비밀번호 인코딩을 업그레이드.
     * @param user 로그인한 사용자객체(준영속 상태)
     * @param rawPassword 사용자가 입력한 원본 비밀번호
     */
    @Transactional
    public void upgradePasswordIfNecessary(User user, String rawPassword) {

        String encodedPassword = user.getPassword();

        // 현재 비밀번호 인코딩이 최신 방식인지 확인
        if (passwordEncoder.upgradeEncoding(encodedPassword)) {
            // 최신 방식이 아니라면, 현재 입력된 비밀번호를 최신 방식으로 다시 인코딩하여 저장
            user.changePassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user); // 준영속상태이기에 save로 영속 상태로 만들고, 변경 사항을 DB에 반영
        }
    }
}
