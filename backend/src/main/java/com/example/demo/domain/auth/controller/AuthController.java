package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.domain.user.dto.CustomUserDetails;
import com.example.demo.global.jwt.service.TokenService;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "Authentication", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDoc{

    private final AuthService authService;
    private final TokenService tokenService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "유효성 검사 실패 또는 이미 존재하는 이메일")
    @ApiResponse(responseCode = "400", description = "이메일인증 미실시")
    @PostMapping("/signup")
    public RsData<?> signUp(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 성공");
    }

    @Operation(summary = "토큰 재발급", description = "HttpOnly 쿠키에 담긴 Refresh Token을 사용하여 Access Token과 Refresh Token을 재발급합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            headers = {
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Authorization", description = "새로 발급된 Access Token", schema = @Schema(type = "string")),
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Set-Cookie", description = "새로 발급된 Refresh Token (HttpOnly 쿠키)", schema = @Schema(type = "string"))
            })
    @ApiResponse(responseCode = "400", description = "쿠키에 Refresh Token이 없음")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    @PostMapping("/reissue")
    public ResponseEntity<RsData<?>> reissue(
            @Parameter(hidden = true) @CookieValue("refreshToken") String refreshToken) {

        TokenResponse tokens = tokenService.reissueTokens(refreshToken);

        ResponseCookie refreshCookie = tokenService.setRefreshTokenToCookie(tokens.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(new RsData<>("200", "토큰 재발행 완료되었습니다."));
    }


    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공")
    @ApiResponse(responseCode = "400", description = "현재 비밀번호 불일치 또는 유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "인증 실패: 로그인이 필요합니다.")
    @PatchMapping("/password")
    public RsData<?> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequest passwordChangeRequest){
        authService.changePassword(userDetails.getId(), passwordChangeRequest);
        return new RsData<>("200", "비밀번호 변경 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 초기화", description = "이메일 인증 후 새 비밀번호로 재설정합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 초기화 성공")
    @ApiResponse(responseCode = "400", description = "이메일 미인증 또는 유효성 검사 실패")
    @ApiResponse(responseCode = "401", description = "인증 실패: 로그인이 필요합니다.")
    @PostMapping("/password")
    public RsData<?> resetPassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        authService.resetPassword(userDetails.getId(),resetPasswordRequest);
        return new RsData<>("200", "비밀번호 초기화 완료되었습니다.");
    }

    @Operation(summary = "관리자 전용 API 테스트", description = "ADMIN 권한을 가진 사용자만 접근 가능한 테스트용 API입니다.")
    @ApiResponse(responseCode = "200", description = "접근 성공")
    @ApiResponse(responseCode = "403", description = "접근 권한 없음 (ADMIN이 아님)")
    @ApiResponse(responseCode = "401", description = "인증 실패: 로그인이 필요합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminOnly() {
        return "관리자 전용 페이지";
    }


    @Override
    public ResponseEntity<RsData<?>> login() {
        return null;
    }

    @Override
    public ResponseEntity<RsData<?>> logout() {
        return null;
    }
}
