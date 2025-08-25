package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.exception.InvalidOrExpiredVerificationCodeException;
import com.example.demo.domain.mail.exception.MessagingFailException;
import com.example.demo.domain.mail.exception.VerificationCodeNotRequestedException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSendingService emailSendingService;
    private final RedisTokenRepository redisTokenRepository;

    private static final long AUTH_CODE_EXPIRATION_MILLIS = 1800000L; // 30분

    @Transactional
    public void sendVerificationCode(String email) {
        String code = createAuthCode();
        redisTokenRepository.saveVerificationCode(email, code, AUTH_CODE_EXPIRATION_MILLIS);
        try {
            emailSendingService.sendAuthCodeEmail(email, code);
        } catch (Exception e) {
            throw new MessagingFailException(e.getMessage());
        }
    }

    @Transactional
    public void verifyEmail(String email, String code) {

        // 이메일 인증코드를 보낸 적이 없다면 예외 처리
        if (!redisTokenRepository.existsVerificationCode(email)) {
            throw new VerificationCodeNotRequestedException();
        }

        String savedCode = redisTokenRepository.getVerificationCode(email);
        if (savedCode == null || !savedCode.equals(code)) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        // 인증 성공: Redis에 인증된 이메일 저장 (TTL 20분 설정)
        redisTokenRepository.setVerifiedEmail(email);  // "verified:email" -> "true"

        redisTokenRepository.deleteVerificationCode(email); // 인증 완료 후 코드 삭제
    }

    private String createAuthCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(900000) + 100000);
    }
}
