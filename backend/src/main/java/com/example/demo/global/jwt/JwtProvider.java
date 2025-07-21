package com.example.demo.global.jwt;

import com.example.demo.global.exception.CustomJwtException;
import com.example.demo.global.jwt.exception.JwtInvalidSignatureException;
import com.example.demo.global.jwt.exception.JwtMalformedTokenException;
import com.example.demo.global.jwt.exception.JwtTokenExpiredException;
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
        try {
            // 1. 파서 빌더 생성(내부적으로는 DefaultJwtParserBuilder 객체를 반환)
            return Jwts.parser()
            // 2. 검증용 키 등록(JWT의 Signature를 검증)
                    .verifyWith(getKey())
            // 3. 파서 객체 완성
                    .build()
            // 4. 토큰 파싱 + 서명 검증
                    // token 문자열을 . 기준으로 세 조각(Header, Payload, Signature)으로 나눔
                    //Header의 alg 값을 보고 적절한 검증 방식 선택
                    //.verifyWith(getKey())에서 지정한 키로 Signature가 유효한지 검증
                    //검증에 성공하면 → Jws<Claims> 타입 객체 반환
                    //Jws = JSON Web Signature (서명 포함된 JWT)
                    //Claims = JWT payload에 담긴 클레임들
                    //Jws<Claims>는 Header + Payload + Signature를 모두 포함한 검증된 JWT 객체
                    .parseSignedClaims(token);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new JwtInvalidSignatureException();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new JwtTokenExpiredException();
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new JwtMalformedTokenException();
        } catch (JwtException | IllegalArgumentException e) {
            // 더 포괄적인 JWT 관련 예외 처리
            throw new CustomJwtException("유효하지 않은 토큰입니다.", "401");
        }
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
            parse(token);
            return true;
        } catch (CustomJwtException e) {
            return false;
        }
    }

}
