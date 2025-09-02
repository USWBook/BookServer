package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.dto.CustomUserDetails;
import com.example.demo.global.jwt.JwtProvider;
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
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public RsData<?> signUp(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 성공");
    }

    //@PostMapping("/login")
//    public ResponseEntity<RsData<TokenResponse>> login(@RequestBody @Valid LoginRequest request) {
//        TokenResponse tokens = authService.login(request);
//
//        // HttpOnly 쿠키 설정
//        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
//                .httpOnly(true) // JavaScript에서 접근 불가.
//                //.secure(true) // HTTPS 환경에서만 활성화
//                .path("/")
//                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenExpirationInMillis()))
//                .sameSite("Strict") // 또는 Lax/None
//                .build();
//
//        return  ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
//                .body(new RsData<>("200", "로그인 완료되었습니다."));
//    }

    //@PostMapping("/logout")
    public ResponseEntity<RsData<?>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // 리프레쉬 토큰 안받고 레디스에서 뒤지도록 하였음 => 어차피 토큰 삭제하는 과정에서 db접근이 일어나기에 차라리 리프레쉬 토큰이 통신상 노출이 덜 되는 방향으로 바꿈
        authService.logout(authHeader, userDetails.getUsername());

        // refreshToken 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                //.secure(true)
                .path("/")
                .maxAge(0) // 쿠키 즉시 만료
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new RsData<>("200", "로그아웃 완료되었습니다."));
    }

    // @CookieValue를 통해 클라이언트가 보내는 HttpOnly 쿠키에서 Refresh Token을 읽음
    @PostMapping("/reissue")
    public ResponseEntity<RsData<TokenResponse>> reissue(@CookieValue("refreshToken") String refreshToken) {
        TokenResponse tokens = authService.reissue(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                //.secure(true)  // HTTPS 환경에서만 true, 로컬 개발시 false
                .path("/")
                .maxAge(Duration.ofMillis(jwtProvider.getRefreshTokenExpirationInMillis()))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(new RsData<>("200", "토큰 재발행 완료되었습니다."));
    }

    @PostMapping("change-password")
    public RsData<?> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid PasswordChangeRequest passwordChangeRequest){
        authService.changePassword(userDetails.getUsername(),passwordChangeRequest);
        return new RsData<>("200", "비밀번호 변경 완료되었습니다.");
    }

    @PostMapping("reset-password")
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
