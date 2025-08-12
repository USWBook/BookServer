package com.example.demo.global.config;

import com.example.demo.global.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod") // prod 프로필이 아닐 때(=디폴트 또는 local일 때) 활성화
public class LocalConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Bean
    public JwtProvider jwtProvider() {
        // 설정 파일에서 직접 읽은 값으로 JwtProvider를 생성
        return new JwtProvider(secret, accessExpiration, refreshExpiration);
    }
}
