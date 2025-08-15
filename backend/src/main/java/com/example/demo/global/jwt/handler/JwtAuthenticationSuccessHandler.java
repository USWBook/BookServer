package com.example.demo.global.jwt.handler;

import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.redis.repository.RedisTokenRepository;
import com.example.demo.domain.user.dto.UserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisTokenRepository redisTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String email = userPrincipal.getUsername();
        Role role = userPrincipal.getRole();

        String accessToken = jwtProvider.generateAccessToken(email, role);
        String refreshToken = jwtProvider.generateRefreshToken(email, role);

        // Redis에 refresh 저장 (화이트리스트)
        redisTokenRepository.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        // refreshToken HttpOnly 쿠키로 세팅
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                // .secure(true) // 운영에서 true
                .path("/")
                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenExpirationInMillis()).getSeconds())
                .sameSite("Lax")
                .build();

        // AccessToken은 Authorization 헤더로 전달
        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        // 응답 바디 (선택) — 간단한 성공 메시지
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"200\",\"message\":\"로그인 성공\"}");
    }
}

