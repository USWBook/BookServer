package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
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
        return new RsData<>("200", "회원가입 성공");
    }

    @PostMapping("/login")
    public RsData<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return new RsData<>("200", "로그인 완료되었습니다.", tokens);
    }

    @PostMapping("/logout")
    public RsData<?> logout(@RequestHeader(value = "Authorization",required = false) String authHeader) {
        authService.logout(authHeader);
        return new RsData<>("200", "로그아웃 완료되었습니다.");
    }

    @PostMapping("/reissue")
    public RsData<TokenResponse> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokens = authService.reissue(refreshToken);
        return new RsData<>("200", "토큰 재발행 완료되었습니다.", tokens);
    }

    @PostMapping("change-password")
    public RsData<?> changePassword(@RequestHeader(value = "Authorization",required = false) String authHeader,@RequestBody @Valid PasswordChangeRequest passwordChangeRequest){
        authService.changePassword(authHeader,passwordChangeRequest);
        return new RsData<>("200", "비밀번호 변경 완료되었습니다.");
    }

    @PostMapping("reset-password")
    public RsData<?> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        authService.resetPassword(resetPasswordRequest);
        return new RsData<>("200", "비밀번호 초기화 완료되었습니다.");
    }

}
