package com.example.demo.domain.auth.controller;

import com.example.demo.domain.auth.dto.request.LoginRequest;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

// 로그인 로그아웃은 필터에서 잡기 때문에 스웨거를 적용시켜줄 인터페이스를 따로 만들었으며
// auth컨트롤러랑 같은 태그이기에 컨트롤러에서 임플리먼트 시켜줘야함
@Tag(name = "Authentication", description = "인증/인가 API")
@RequestMapping("/api/auth")
public interface AuthControllerDoc {

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. (Spring Security Filter에서 처리)")
    @RequestBody(description = "로그인 요청 DTO", required = true, content = @Content(schema = @Schema(implementation = LoginRequest.class)))
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            headers = {
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Authorization", description = "발급된 Access Token (Bearer 포함)", schema = @Schema(type = "string")),
                    @io.swagger.v3.oas.annotations.headers.Header(name = "Set-Cookie", description = "발급된 Refresh Token (HttpOnly 쿠키 -> prefix: refresh=)", schema = @Schema(type = "string"))
            },
            content = @Content(schema = @Schema(implementation = RsData.class)))
    @ApiErrorResponse(
            description = "유효성 검사 실패",
            exampleName = "ValidationFailure",
            exampleValue = "{\"code\": \"400\", \"message\": \"비밀번호는 8자 이상, 16자 이하로 입력해주세요.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "403",
            description = "현재 비밀번호 불일치 또는 유효성 검사 실패.",
            exampleName = "InvalidPassword",
            exampleValue = "{\"code\": \"403\", \"message\": \"비밀번호가 일치하지 않습니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "존재하지 않는 사용자",
            exampleName = "UserNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"존재하지 않는 사용자입니다.\", \"data\": null}"
    )
    @PostMapping("/login")
    ResponseEntity<RsData<?>> login();


    @Operation(summary = "로그아웃", description = "서버에서 Access Token과 Refresh Token을 만료 처리합니다. (Spring Security Filter에서 처리)")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(implementation = RsData.class)))
    @ApiErrorResponse(
            responseCode = "401",
            description = "모든 Token 이슈는 토큰이 만료되었다고 응답\n" + "로그아웃시 access 생명주기가 남아있다면 블랙리스트 만료되었으면 그냥 넘기고\n" + "refresh는 레디스에서 삭제 하도록 하였는데",
            exampleName = "JwtTokenExpired",
            exampleValue = "{\"code\": \"401\", \"message\": \"토큰이 만료되었습니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            description = "유효성 검사 실패",
            exampleName = "ValidationFailure",
            exampleValue = "{\"code\": \"400\", \"message\": \"비밀번호는 8자 이상, 16자 이하로 입력해주세요.\", \"data\": null}"
    )
    @PostMapping("/logout")
    ResponseEntity<RsData<?>> logout();
}


