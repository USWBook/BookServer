package com.example.demo.global.redis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    // KEY-VALUE 형태로 Redis에 데이터 저장
    public void setData(String key, String value, long durationMillis) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(durationMillis));
    }

    // KEY로 VALUE 조회
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // KEY에 해당하는 데이터 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}