package com.example.demo.global.jwt.handler;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.user.role.Role;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.domain.user.dto.CustomUserDetails;
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

    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        String email = userPrincipal.getUsername();
        Role role = userPrincipal.getRole();


        TokenResponse tokenResponse = tokenService.generateTokens(userPrincipal.getId(),email, role);

        // String accessToken = jwtProvider.generateAccessToken(email, role);
        // String refreshToken = jwtProvider.generateRefreshToken(email, role);

        // Redis에 refresh 저장 (화이트리스트)
        //redisTokenRepository.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpirationInMillis());

        // refreshToken HttpOnly 쿠키로 세팅
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true) // 운영에서 true
                .path("/")
                .maxAge(Duration.ofMillis(tokenService.getRefreshExpirationInMillis()).getSeconds())
                .sameSite("None")
                .build();

        // AccessToken은 Authorization 헤더로 전달
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken());
        response.addHeader("Access-Control-Expose-Headers", "Authorization, Content-Disposition, Set-Cookie");

        // 응답 바디 (선택) — 간단한 성공 메시지
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"200\",\"message\":\"로그인 성공\"}");
    }
}

