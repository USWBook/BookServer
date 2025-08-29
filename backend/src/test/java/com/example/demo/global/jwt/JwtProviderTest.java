package com.example.demo.global.jwt;

import com.example.demo.domain.user.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

//    @BeforeEach
//    void setUp() {
//        // 테스트용 시크릿 키와 만료 시간 (값은 자유)
//        String testSecret = "ThisIsTestSecretKeyForUswBookProjectSoDontUseInProduction!";
//        long accessExpiration = 3600L;  // 1시간
//        long refreshExpiration = 604800L; // 7일
//        jwtProvider = new JwtProvider(testSecret, accessExpiration, refreshExpiration);
//    }

    @Test
    @DisplayName("액세스 토큰 생성 및 파싱 테스트")
    public void generateAndParseAccessToken() {
        // given
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;

        // when
        String accessToken = jwtProvider.generateAccessToken(email, role);

        // then
        assertThat(accessToken).isNotNull();

        Jws<Claims> claims = assertDoesNotThrow(() -> jwtProvider.parse(accessToken));
        assertThat(claims.getPayload().getSubject()).isEqualTo(email);
        assertThat(claims.getPayload().get("role", String.class)).isEqualTo(role.name());
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 유효성 검증 테스트")
    public void generateAndValidateRefreshToken() {
        // given
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;

        // when
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(jwtProvider.isValid(refreshToken)).isTrue();
    }

//    @Test
//    @DisplayName("만료된 토큰 검증 테스트")
//    public void validateExpiredToken() throws InterruptedException {
//        // given
//        // 만료 시간을 1초로 설정한 새로운 JwtProvider 생성
//        JwtProvider expiredJwtProvider = new JwtProvider("testSecret", 1L, 1L);
//        String email = "test@suwon.ac.kr";
//        Role role = Role.USER;
//
//        // when
//        String expiredToken = expiredJwtProvider.generateAccessToken(email, role);
//        Thread.sleep(1100); // 1.1초 대기하여 토큰을 만료시킴
//
//        // then
//        // isValid는 내부적으로 예외를 잡으므로 false를 반환해야 함
//        assertThat(expiredJwtProvider.isValid(expiredToken)).isFalse();
//    }
}