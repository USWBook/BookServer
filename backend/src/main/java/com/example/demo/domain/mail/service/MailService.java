package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.entity.MailStatus;
import com.example.demo.domain.mail.exception.InvalidOrExpiredVerificationCodeException;
import com.example.demo.domain.mail.exception.MessagingFailException;
import com.example.demo.domain.mail.exception.VerificationCodeNotRequestedException;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSendingService emailSendingService;
    private final RedisTokenRepository redisTokenRepository;

    private static final long AUTH_CODE_EXPIRATION_MILLIS = 1800000L; // 30분

    @Transactional
    public void sendVerificationCode(String email) throws Exception {
        String code = createAuthCode();
        redisTokenRepository.saveVerificationCode(email, code, AUTH_CODE_EXPIRATION_MILLIS);

        // 1. 비동기 작업 호출 직후, 상태를 PENDING으로 저장
        redisTokenRepository.saveMailStatus(email, MailStatus.PENDING);

        CompletableFuture<Void> emailResult = emailSendingService.sendAuthCodeEmail(email, code);

        emailResult.thenAccept(voidResult -> {
            // 작업 성공 시, 상태를 SUCCESS로 업데이트
            redisTokenRepository.saveMailStatus(email, MailStatus.SUCCESS);
            System.out.println(email + " 주소로 메일 발송 성공!");
        });

        emailResult.exceptionally(ex -> {
            //  작업 실패 시, 상태를 FAILED로 업데이트
            redisTokenRepository.saveMailStatus(email, MailStatus.FAILED);
            System.err.println(email + " 주소로 메일 발송 실패: " + ex.getMessage());
            return null;
        });
    }

    //  상태 조회를 위한  메서드
    public MailStatus getMailStatus(String email) {
        return redisTokenRepository.getMailStatus(email);
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
