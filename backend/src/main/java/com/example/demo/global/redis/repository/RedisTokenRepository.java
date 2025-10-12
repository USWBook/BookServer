package com.example.demo.global.redis.repository;

import com.example.demo.domain.mail.entity.MailStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class RedisTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenRepository(@Qualifier("authRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String EMAIL_AUTH_PREFIX = "auth:code:";
    private static final String VERIFIED_EMAIL_PREFIX = "verified:";
    private static final String EMAIL_STATUS_PREFIX = "mail_status:";

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken, long expirationInMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                Duration.ofMillis(expirationInMillis)
        );
        log.info("[Redis] 리프레쉬 토큰 저장: {}, 유효 시간: {}ms", email, expirationInMillis);
    }

    // 리프레시 토큰값 가져옴
    public Optional<String> getRefreshToken(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_PREFIX + email));
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }

    // 토큰 블랙리스트로 등록
    public void blacklistToken(String Token, long expirationMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + Token,
                "logout",
                Duration.ofMillis(expirationMillis)
        );
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    // 이메일 인증코드 저장함
    public void saveVerificationCode(String email, String code, long expirationMillis) {
        redisTemplate.opsForValue().set(EMAIL_AUTH_PREFIX + email, code, expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 이메일이 할당받은 인증코드 가져옴
    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get(EMAIL_AUTH_PREFIX + email);
    }

    // 이메일 인증 성공 시 검증 완료된 이메일이라고 20분간 저장
    public void setVerifiedEmail(String email) {
        String key = VERIFIED_EMAIL_PREFIX + email;
        redisTemplate.opsForValue().set(key, "true", Duration.ofMinutes(20));
    }

    // 인증코드 값 비교
    public boolean isVerifiedEmail(String email) {
        String key = VERIFIED_EMAIL_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);
        return "true".equals(value);
    }

    // 인증 코드를 보낸 이메일인지 확인
    public boolean existsVerificationCode(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(EMAIL_AUTH_PREFIX + email));
    }

    // 인증코드 검증 완료된 이메일 삭제
    public void deleteVerifiedEmail(String email) {
        String key = VERIFIED_EMAIL_PREFIX + email;
        redisTemplate.delete(key);
    }

    // 이메일 인증 완료 후 인증코드 삭제
    public void deleteVerificationCode(String email) {
        redisTemplate.delete(EMAIL_AUTH_PREFIX + email);
    }

    // 레디스에 토큰이 존재하는지 검증
    public boolean existsRefreshToken(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_PREFIX + email));
    }

    // 메일 발송 상태 저장 (20분 유효시간)
    public void saveMailStatus(String email, MailStatus status) {
        String key = EMAIL_STATUS_PREFIX + email;
        redisTemplate.opsForValue().set(key, status.name(), Duration.ofMinutes(20));
    }

    // 메일 발송 상태 조회
    public MailStatus getMailStatus(String email) {
        String key = EMAIL_STATUS_PREFIX + email;
        String status = (String) redisTemplate.opsForValue().get(key);
        return status != null ? MailStatus.valueOf(status) : null;
    }
}