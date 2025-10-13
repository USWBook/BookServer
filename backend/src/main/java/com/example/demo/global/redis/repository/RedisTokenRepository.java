package com.example.demo.global.redis.repository;

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
public class RedisTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenRepository(@Qualifier("authRedisTemplate") StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";


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

    // 레디스에 토큰이 존재하는지 검증
    public boolean existsRefreshToken(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_PREFIX + email));
    }
}