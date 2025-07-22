package com.example.demo.domain.mail.service;

import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.global.redis.util.RedisUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import com.example.demo.domain.auth.exception.MemberNotFoundException;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSendingService emailSendingService;
    private final RedisTokenRepository redisTokenRepository;
    private final UserRepository userRepository;

    private static final long AUTH_CODE_EXPIRATION_MILLIS = 180000L; // 3분

    @Transactional
    public void sendVerificationCode(String email) {
        String code = createAuthCode();
        redisTokenRepository.saveVerificationCode(email, code, AUTH_CODE_EXPIRATION_MILLIS);
        emailSendingService.sendAuthCodeEmail(email, code);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        String savedCode = redisTokenRepository.getVerificationCode(email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        User user = userRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        user.completeSignUp(); // 유저 상태를 ACTIVE로 변경

        redisTokenRepository.deleteVerificationCode(email); // 인증 완료 후 코드 삭제
    }

    private String createAuthCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(900000) + 100000);
    }
}
