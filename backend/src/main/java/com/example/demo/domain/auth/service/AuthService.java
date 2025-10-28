package com.example.demo.domain.auth.service;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.exception.*;
import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.major.entity.Major;
import com.example.demo.domain.major.exception.MajorNotFoundException;
import com.example.demo.domain.major.repository.MajorRepository;
import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.enums.UserStatus;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.redis.repository.RedisMailRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MajorRepository majorRepository;
    private final RedisMailRepository redisMailRepository;


    @Transactional
    public void signUp(SignUpRequest request) {

        userRepository.findByEmail(request.email())
                // 1. 이메일에 해당하는 유저가 존재할 경우 (Optional이 비어있지 않을 경우)
                .ifPresentOrElse(
                        user -> {
                            // 1-1. 유저가 존재하지만, 탈퇴 상태(WITHDRAWAL)인 경우
                            if (user.getStatus() == UserStatus.WITHDRAWAL) {
                                reSignUp(user, request); // 재가입 로직 실행
                            } else {
                                // 1-2. 유저가 존재하며, 탈퇴 상태가 아닌 경우 (ACTIVE, BANNED 등)
                                throw new ExistEmailSignUpException();
                            }
                        },
                        // 2. 이메일에 해당하는 유저가 존재하지 않을 경우 (Optional이 비어있을 경우)
                        () -> firstSignUp(request)
                );

    }


    @Transactional
    public void reSignUp(User withdrawnUser , SignUpRequest request) {

        // 이메일 인증을 다시 했는지 확인하는 로직이 필요하다면 여기에 추가
        if (!redisMailRepository.isVerifiedEmail(request.email(), EmailAuthPurpose.SIGN_UP)) {
            throw new EmailNotVerifiedException();
        }

        Major major = majorRepository.findById(request.majorId())
                .orElseThrow(MajorNotFoundException::new);

        // 기존 User 엔티티의 상태를 업데이트 (새로 생성하는 것이 아님)
        withdrawnUser.reactivate(
                 passwordEncoder.encode(request.password()),
                 request.name(),
                 request.studentId(),
                 major,
                Grade.fromValue(request.grade()),
                Semester.fromValue(request.semester()),
                Role.USER
                );
        // 프로필 이미지 재설정 (옵셔널)
        if (request.profileImageUrl() != null && !request.profileImageUrl().isEmpty()) {
            withdrawnUser.updateProfileImage(request.profileImageUrl());
        }

        redisMailRepository.deleteVerifiedEmail(request.email(), EmailAuthPurpose.SIGN_UP);
    }

    @Transactional
    public void firstSignUp(SignUpRequest request) {

        // 이메일 인증 여부 확인
        if (!redisMailRepository.isVerifiedEmail(request.email(), EmailAuthPurpose.SIGN_UP)) {
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
                .grade(Grade.fromValue(request.grade()))
                .semester(Semester.fromValue(request.semester()))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .profileImageUrl(request.profileImageUrl())
                .build();

        userRepository.save(user);

        // 인증 상태 삭제 (더 이상 필요 없으므로)
        redisMailRepository.deleteVerifiedEmail(request.email(), EmailAuthPurpose.SIGN_UP);
    }


    @Transactional
    public void changePassword(UUID id ,@Valid PasswordChangeRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void resetPassword(@Valid ResetPasswordRequest request) {

        // 이메일 인증 여부 확인
        if (!redisMailRepository.isVerifiedEmail(request.email(), EmailAuthPurpose.PASSWORD_RESET)) {
            throw new EmailNotVerifiedException();
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        user.changePassword(passwordEncoder.encode(request.newPassword()));

        redisMailRepository.deleteVerifiedEmail(request.email(), EmailAuthPurpose.PASSWORD_RESET);

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
