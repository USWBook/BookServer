package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.dto.CustomUserDetails;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    @PostMapping("/signup")
    public RsData<?> signUp(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 성공");
    }

    // @CookieValue를 통해 클라이언트가 보내는 HttpOnly 쿠키에서 Refresh Token을 읽음
    @PostMapping("/reissue")
    public ResponseEntity<RsData<TokenResponse>> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokens = authService.reissue(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)  // HTTPS 환경에서만 true, 로컬 개발시 false
                .path("/")
                .maxAge(Duration.ofMillis(tokenService.getRefreshExpirationInMillis()))
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(new RsData<>("200", "토큰 재발행 완료되었습니다."));
    }

    @PatchMapping("password")
    public RsData<?> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid PasswordChangeRequest passwordChangeRequest){
        authService.changePassword(userDetails.getUsername(),passwordChangeRequest);
        return new RsData<>("200", "비밀번호 변경 완료되었습니다.");
    }

    @PostMapping("password")
    public RsData<?> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        authService.resetPassword(resetPasswordRequest);
        return new RsData<>("200", "비밀번호 초기화 완료되었습니다.");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminOnly() {
        return "관리자 전용 페이지";
    }
}
