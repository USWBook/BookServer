package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.PasswordChangeRequest;
import com.example.demo.domain.auth.dto.request.ResetPasswordRequest;
import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.dto.response.TokenResponse;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiForbiddenResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.annotation.swagger.ApiUnauthorizedResponse;
import com.example.demo.global.security.userdetails.CustomUserDetails;
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

@Tag(name = "Authentication", description = "인증/인가 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDoc{

    private final AuthService authService;
    private final TokenService tokenService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiSuccessResponse(description = "회원가입 성공")
    @ApiErrorResponse(
            responseCode = "409",
            description = "이미 회원가입 되어 있는 이메일로 회원가입 시도",
            exampleName = "ExistEmailSignUp",
            exampleValue = "{\"code\": \"409\", \"message\": \"이미 회원가입 되어 있는 이메일 입니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "403",
            description = "이메일 인증을 완료 하십시오.",
            exampleName = "EmailNotVerified",
            exampleValue = "{\"code\": \"403\", \"message\": \"이메일 인증을 완료 하십시오\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "찾을 수 없는 전공",
            exampleName = "MajorNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"존재하지 않는 전공입니다.\", \"data\": null}"
    )
    @PostMapping("/signup")
    public RsData<?> signUp(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return RsData.of("200", "회원가입 성공");
    }

    @Operation(summary = "토큰 재발급", description = "HttpOnly 쿠키에 담긴 Refresh Token을 사용하여 Access Token과 Refresh Token을 재발급합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
            headers = {
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Authorization", description = "새로 발급된 Access Token", schema = @Schema(type = "string")),
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Set-Cookie", description = "새로 발급된 Refresh Token (HttpOnly 쿠키)", schema = @Schema(type = "string"))
            })
    @ApiErrorResponse(
            responseCode = "400",
            description = "쿠키에 Refresh Token이 없음",
            exampleName = "handleMissingRequestCookie",
            exampleValue = "{\"code\": \"400\", \"message\": \"필수 쿠키 '%s'가 요청에 포함되지 않았습니다\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "401",
            description = "모든 Refresh Token 이슈는 토큰이 만료되었다고 응답",
            exampleName = "JwtTokenExpired",
            exampleValue = "{\"code\": \"401\", \"message\": \"토큰이 만료되었습니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "요청준 사용자를 db에서 찾지 못함.",
            exampleName = "UserNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다..\", \"data\": null}"
    )
    @PostMapping("/reissue")
    public ResponseEntity<RsData<?>> reissue(
            @Parameter(hidden = true) @CookieValue("refreshToken") String refreshToken) {

        TokenResponse tokens = tokenService.reissueTokens(refreshToken);

        ResponseCookie refreshCookie = tokenService.setRefreshTokenToCookie(tokens.getRefreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(RsData.of("200", "토큰 재발행 완료되었습니다."));
    }


    @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
    @ApiSuccessResponse(description = "비밀번호 변경 성공")
    @ApiErrorResponse(
            responseCode = "400",
            description = "현재 비밀번호 불일치 또는 유효성 검사 실패.",
            exampleName = "InvalidPassword",
            exampleValue = "{\"code\": \"403\", \"message\": \"비밀번호가 일치하지 않습니다.\", \"data\": null}"
    )
    @ApiUnauthorizedResponse
    @PatchMapping("/password")
    public RsData<?> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid PasswordChangeRequest passwordChangeRequest){
        authService.changePassword(userDetails.getId(), passwordChangeRequest);
        return RsData.of("200", "비밀번호 변경 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 초기화", description = "이메일 인증 후 새 비밀번호로 재설정합니다.")
    @ApiSuccessResponse( description = "비밀번호 초기화 성공")
    @ApiErrorResponse(
            responseCode = "403",
            description = "이메일 인증을 완료 하십시오.",
            exampleName = "EmailNotVerified",
            exampleValue = "{\"code\": \"403\", \"message\": \"이메일 인증을 완료 하십시오\", \"data\": null}"
    )

    @ApiErrorResponse(
            responseCode = "404",
            description = "존재하지 않는 사용자",
            exampleName = "UserNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다.\", \"data\": null}"
    )
    @ApiUnauthorizedResponse
    @PostMapping("/password")
    public RsData<?> resetPassword(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ResetPasswordRequest resetPasswordRequest){
        authService.resetPassword(userDetails.getId(),resetPasswordRequest);
        return RsData.of("200", "비밀번호 초기화 완료되었습니다.");
    }

    @Operation(summary = "관리자 전용 API 테스트", description = "ADMIN 권한을 가진 사용자만 접근 가능한 테스트용 API입니다.")
    @ApiResponse( description = " 관리자 권한으로 접근 성공")
    @ApiUnauthorizedResponse //  로그인 필요 명시 401
    @ApiForbiddenResponse    //  권한 필요 명시 403
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
