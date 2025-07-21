package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.request.TokenResponse;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.global.jwt.JwtProvider;
import com.example.demo.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public RsData<?> signUp(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 완료되었습니다.");
    }

    @PostMapping("/login")
    public RsData<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return new RsData<>("200", "로그인 완료되었습니다.", tokens);
    }

    @PostMapping("/logout")
    public RsData<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.extractEmail(token);
        authService.logout(token, email);
        return new RsData<>("200", "로그아웃 완료되었습니다.");
    }

    @PostMapping("/reissue")
    public RsData<TokenResponse> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokens = authService.reissue(refreshToken);
        return new RsData<>("200", "토큰 재발행 완료되었습니다.", tokens);
    }

}
