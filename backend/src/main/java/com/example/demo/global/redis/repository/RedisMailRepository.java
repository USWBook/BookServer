package com.example.demo.global.redis.repository;

import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.mail.enums.MailStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class RedisMailRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisMailRepository(@Qualifier("authRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String EMAIL_AUTH_PREFIX = "auth:code:"; // 상태코드
    private static final String VERIFIED_EMAIL_PREFIX = "verified:"; // 인증여부
    private static final String EMAIL_STATUS_PREFIX = "mail_status:"; // 메일전송 상태
    private static final String MAIL_REQUEST_LOCK_PREFIX = "mail_lock:"; // 메일 재전송 여부

    private String generateKey(String prefix, EmailAuthPurpose purpose, String email) {
        return prefix + purpose.name() + ":" + email;
    }

    // 이메일 인증코드 저장함
    public void saveVerificationCode(String email, String code, long expirationMillis, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_AUTH_PREFIX, purpose, email);
        redisTemplate.opsForValue().set(key, code, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 이메일이 할당받은 인증코드 가져옴
    public Optional<String> getVerificationCode(String email, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_AUTH_PREFIX, purpose, email);
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    // 이메일 인증 성공 시 검증 완료된 이메일이라고 20분간 저장
    public void setVerifiedEmail(String email, EmailAuthPurpose purpose) {
        String key = generateKey(VERIFIED_EMAIL_PREFIX, purpose, email);
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(20));
    }

    // 인증코드 값 비교
    public boolean isVerifiedEmail(String email, EmailAuthPurpose purpose) {
        String key = generateKey(VERIFIED_EMAIL_PREFIX, purpose, email);
        String value = redisTemplate.opsForValue().get(key);
        return "true".equals(value);
    }

    // 인증 코드를 보낸 이메일인지 확인
    public boolean existsVerificationCode(String email, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_AUTH_PREFIX, purpose, email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 인증코드 검증 완료된 이메일 삭제
    public void deleteVerifiedEmail(String email, EmailAuthPurpose purpose) {
        String key = generateKey(VERIFIED_EMAIL_PREFIX, purpose, email);
        redisTemplate.delete(key);
    }

    // 이메일 인증 완료 후 인증코드 삭제
    public void deleteVerificationCode(String email, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_AUTH_PREFIX, purpose, email);
        redisTemplate.delete(key);
    }

    // 메일 발송 상태 저장 (20분 유효시간)
    public void saveMailStatus(String email, MailStatus status, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_STATUS_PREFIX, purpose, email);
        redisTemplate.opsForValue().set(key, status.name(), Duration.ofMinutes(20));
    }

    // 메일 발송 상태 삭제
    public void deleteMailStatus(String email, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_STATUS_PREFIX, purpose, email);
        redisTemplate.delete(key);
    }

    // 메일 발송 상태 조회
    public Optional<MailStatus> getMailStatus(String email, EmailAuthPurpose purpose) {
        String key = generateKey(EMAIL_STATUS_PREFIX, purpose, email);
        String status = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(status).map(MailStatus::valueOf);
    }

    /**
     * 메일 재요청 잠금을 1분 동안 설정.
     * @param email 잠금을 설정할 이메일
     * @param purpose 인증 목적
     */
    public void saveMailRequestLock(String email, EmailAuthPurpose purpose) {
        String key = generateKey(MAIL_REQUEST_LOCK_PREFIX, purpose, email);
        redisTemplate.opsForValue().set(key, "locked", Duration.ofMinutes(1));
    }

    /**
     * 해당 이메일의 메일 재요청이 잠겨있는지 확인.
     * @param email 확인할 이메일
     * @param purpose 인증 목적
     * @return 잠겨있으면 true, 아니면 false
     */
    public boolean hasMailRequestLock(String email, EmailAuthPurpose purpose) {
        String key = generateKey(MAIL_REQUEST_LOCK_PREFIX, purpose, email);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
