package com.example.demo.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.example.demo.domain.user.role.Role;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;


    @Value("${jwt.access-expiration}")
    private long accessExpirationInSeconds;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationInSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getKey(){
        return this.key;
    }

    public String generateAccessToken(String email,Role role){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpirationInSeconds * 1000);

        return Jwts.builder()
                .subject(email)
                .claim("role",role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String email, Role role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationInSeconds * 1000);

        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token); // 유효성 검사 수행
    }

    public String extractEmail(String token) {
        return parse(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }


    public String extractEmailIgnoreExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)// 유효성 검사 수행
                .getPayload()
                .getSubject();
    }

    public long getRefreshTokenExpirationInMillis() {
        return refreshExpirationInSeconds*1000;
    }

    public long getAccessTokenRemainingTime(String token) {
        Date exp = parse(token).getPayload().getExpiration();
        return (exp.getTime() - System.currentTimeMillis()) / 1000;
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token); // 유효성 검사 수행

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰 만료, 서명 오류, 잘못된 형식 등
            return false;
        }
    }
}
