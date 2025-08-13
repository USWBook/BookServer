package com.example.demo.global.security.handler;

import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Duration;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final TokenService tokenService;

    public LoginSuccessHandler(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TokenResponse tokens = tokenService.generateTokens(userPrincipal.getUsername(), userPrincipal.getRole());

        // accessToken은 Authorization 헤더에
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken());

        // refreshToken은 HttpOnly 쿠키에
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                //.httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(tokenService.getRefreshExpirationInMillis()))
                .sameSite("Strict")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"code\":\"200\",\"message\":\"로그인 성공\"}");
    }
}

