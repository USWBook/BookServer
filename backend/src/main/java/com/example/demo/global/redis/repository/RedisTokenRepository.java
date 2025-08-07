package com.example.demo.global.redis.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
@Slf4j
public class RedisTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisTokenRepository(@Qualifier("authRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String EMAIL_AUTH_PREFIX = "auth:code:";
    private static final String VERIFIED_EMAIL_PREFIX = "verified:";

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
    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }

    // 액세스 토큰 블랙리스트로 등록
    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(expirationMillis)
        );
    }

    // 블랙리스트 확인
    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
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

}