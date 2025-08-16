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

    import javax.crypto.SecretKey;
    import javax.crypto.spec.SecretKeySpec;
    import java.nio.charset.StandardCharsets;
    import java.util.Date;

    import com.example.demo.domain.user.role.Role;
    import jakarta.annotation.PostConstruct;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Component;

    @Component
    public class JwtProvider {

        @Value("${custom.jwt.secret-key}")
        private String secret;


        @Value("${custom.jwt.access-token-expire-seconds}")
        private long accessExpirationInSeconds;

        @Value("${custom.jwt.refresh-token-expire-seconds}")
        private long refreshExpirationInSeconds;

        private final SecretKey key;

        @PostConstruct
        public void init() {
            key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

//        // 개발용 주석임 지우지 말아줘
//        private final SecretKey key;
//        private final long accessExpirationInSeconds;
//        private final long refreshExpirationInSeconds;
//
//        public JwtProvider(@Value("${jwt.secret}") String secret,
//                           @Value("${jwt.access-expiration}") long accessExpirationInSeconds,
//                           @Value("${jwt.refresh-expiration}") long refreshExpirationInSeconds) {
////            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//            this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm()/*"AES"*/);
//            this.accessExpirationInSeconds = accessExpirationInSeconds;
//            this.refreshExpirationInSeconds = refreshExpirationInSeconds;
//        }


        private SecretKey getKey(){
            return this.key;
        }

        /*
             JWT에 만료시간이 있는데 굳이 expirationMillis를 따로 Redis에 넣는 이유
             Redis TTL(Time-To-Live) 설정 때문
             Redis에 저장된 리프레시 토큰은 수동으로 삭제하지 않으면 계속 남아 있기에
             TTL을 설정하지 않으면 Redis에 영구적으로 남음.
             내부적으로는 RedisTemplate.opsForValue().set(key, value, expirationMillis, TimeUnit.MILLISECONDS)
             이게 Redis에 자동으로 만료되도록 설정해주는 핵심
             리프레시 토큰 만료 시간만큼 Redis에 자동으로 남아 있도록 설정하는 역할.
             토큰과 서버 저장소의 만료 시점을 맞추기 위해 필요
             */
        public String generateToken(String email, Role role, String category, long expirationSeconds) {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationSeconds * 1000);

            return Jwts.builder()
                    .subject(email)
                    .claim("role", role.name())
                    .claim("category", category) // Access / Refresh 구분
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(key, Jwts.SIG.HS256)
                    .compact();
        }

        public String generateAccessToken(String email, Role role) {
            return generateToken(email, role, "access", accessExpirationInSeconds);
        }

        public String generateRefreshToken(String email, Role role) {
            return generateToken(email, role, "refresh", refreshExpirationInSeconds);
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

        public Role extractRole(String token) {
            // return parse(token).getPayload().get("role", String.class);
            String roleName = parse(token).getPayload().get("role", String.class);
            return Role.valueOf(roleName); // 문자열 → enum 변환
        }


        public long getRefreshTokenExpirationInMillis() {
            return refreshExpirationInSeconds*1000;
        }

        public long getTokenRemainingTime(String token) {
            Date exp = parse(token).getPayload().getExpiration();
            return (exp.getTime() - System.currentTimeMillis()) / 1000;
        }

        // 토큰 형식 검사
        public boolean isValid(String token) {
            try {
                parse(token);
                return true;
            } catch (CustomJwtException e) {
                return false;
            }
        }


        public Boolean isExpired(String token) {

            return parse(token).getPayload().getExpiration().before(new Date());
        }

    }
