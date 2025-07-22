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
    private static final String EMAIL_AUTH_PREFIX = "email-auth:";

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken, long expirationInMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                Duration.ofMillis(expirationInMillis)
        );
        log.info("[Redis] 인증 코드 저장: {}, 유효 시간: {}ms", email, expirationInMillis);
    }


    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }

    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                Duration.ofMillis(expirationMillis)
        );
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
    }

    public String getVerificationCode(String email) {
        return redisTemplate.opsForValue().get(EMAIL_AUTH_PREFIX + email);
    }

    public void deleteVerificationCode(String email) {
        redisTemplate.delete(EMAIL_AUTH_PREFIX + email);
    }

    public void saveVerificationCode(String email, String code, long expirationMillis) {
        redisTemplate.opsForValue().set("auth:code:" + email, code, expirationMillis, TimeUnit.MILLISECONDS);
    }
}