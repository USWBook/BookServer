package com.example.demo.global.jwt;

import com.example.demo.domain.user.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@DisplayName("JwtProvider 통합 테스트")
@ActiveProfiles("test")
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("액세스 토큰 생성 및 파싱 테스트")
    public void generateAndParseAccessToken() {
        // given
        UUID userId = UUID.randomUUID();
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;

        // when
        String accessToken = jwtProvider.generateAccessToken(userId, email, role);

        // then
        assertThat(accessToken).isNotNull();

        Jws<Claims> claims = assertDoesNotThrow(() -> jwtProvider.parse(accessToken));
        assertThat(claims.getPayload().getSubject()).isEqualTo(email);
        assertThat(claims.getPayload().get("id", String.class)).isEqualTo(userId.toString());
        assertThat(claims.getPayload().get("role", String.class)).isEqualTo(role.name());
        assertThat(claims.getPayload().get("category", String.class)).isEqualTo("access");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 유효성 검증 테스트")
    public void generateAndValidateRefreshToken() {
        // given
        UUID userId = UUID.randomUUID();
        String email = "test@suwon.ac.kr";
        Role role = Role.USER;

        // when
        String refreshToken = jwtProvider.generateRefreshToken(userId, email, role);

        // then
        assertThat(refreshToken).isNotNull();

        Jws<Claims> claims = assertDoesNotThrow(() -> jwtProvider.parse(refreshToken));
        assertThat(claims.getPayload().get("category", String.class)).isEqualTo("refresh");

        // isValid는 내부적으로 parse를 호출하므로, 만료되거나 서명이 다르면 예외를 던집니다.
        // 따라서 true를 반환하는지 확인하는 것만으로도 충분합니다.
        assertThat(jwtProvider.isValid(refreshToken)).isTrue();
    }
}
