package com.example.demo.domain.mail.service;

import com.example.demo.domain.mail.dto.MailRequestOrVerifyDto;
import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.mail.enums.MailStatus;
import com.example.demo.domain.mail.exception.InvalidEmailAuthPurposeException;
import com.example.demo.domain.mail.exception.InvalidOrExpiredVerificationCodeException;
import com.example.demo.domain.mail.exception.TooManyMailRequestException;
import com.example.demo.global.redis.repository.RedisMailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MailService {

    private final EmailSendingService emailSendingService;
    private final RedisMailRepository redisMailRepository;

    private static final long AUTH_CODE_EXPIRATION_MILLIS = 1800000L; // 30분

    @Transactional
    public void sendVerificationCode(MailRequestOrVerifyDto mailRequestOrVerifyDto) throws Exception {
        String code = createAuthCode();
        String email = mailRequestOrVerifyDto.email();
        EmailAuthPurpose purpose = mailRequestOrVerifyDto.purpose();

        // 1분에 한번만 요청 가능하게 잠금확인
        if (redisMailRepository.hasMailRequestLock(email, purpose)) {
            throw new TooManyMailRequestException();
        }

        // 1분에 한번만 요청 가능하게 잠금
        redisMailRepository.saveMailRequestLock(email, purpose);
        // 인증코드 저장
        redisMailRepository.saveVerificationCode(email, code, AUTH_CODE_EXPIRATION_MILLIS, purpose);
        // 비동기 작업 호출하기전 상태를 PENDING으로 설정
        redisMailRepository.saveMailStatus(email, MailStatus.PENDING, purpose);

        CompletableFuture<Void> emailResult = emailSendingService.sendAuthCodeEmail(email, code,purpose.getValue());

        emailResult.thenAccept(voidResult -> {
            redisMailRepository.saveMailStatus(email, MailStatus.SUCCESS, purpose);
            System.out.println(email + " 주소로 [" + purpose + "] 목적의 메일 발송 성공!");
        });

        emailResult.exceptionally(ex -> {
            redisMailRepository.saveMailStatus(email, MailStatus.FAILED, purpose);
            System.err.println(email + " 주소로 [" + purpose + "] 목적의 메일 발송 실패: " + ex.getMessage());
            return null;
        });
    }

    public Optional<MailStatus> getMailStatus(String email, EmailAuthPurpose purpose) {
        return redisMailRepository.getMailStatus(email, purpose);
    }

    @Transactional
    public void verifyEmail(String email, String code, EmailAuthPurpose purpose) {

        // 이메일 인증코드를 보낸 적이 없다면 예외 처리
        if (!redisMailRepository.existsVerificationCode(email, purpose)) {
            throw new InvalidEmailAuthPurposeException(email);
        }

        String savedCode = redisMailRepository.getVerificationCode(email, purpose)
                .orElseThrow(InvalidOrExpiredVerificationCodeException::new);

        if (!savedCode.equals(code)) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        // 인증 성공: Redis에 인증된 이메일 저장 (TTL 20분 설정)
        redisMailRepository.setVerifiedEmail(email, purpose);
        // 인증 성공: Redis에 저장된 인증코드 삭제
        redisMailRepository.deleteVerificationCode(email, purpose);
        // 인증 성공: Redis에 저장된 이메일 전송상태 삭제
        redisMailRepository.deleteMailStatus(email, purpose);
    }

    private String createAuthCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextInt(900000) + 100000);
    }
}