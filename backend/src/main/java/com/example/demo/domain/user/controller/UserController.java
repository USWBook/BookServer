package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.global.security.userdetails.CustomUserDetails;
import com.example.demo.domain.user.response.UserInfoResponse;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RsData.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 불일치)")
    @ApiResponse(responseCode = "401", description = "인증 실패: 로그인이 필요합니다.")
    @GetMapping("/infomation")
    public RsData<?> infomation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse userInfoResponse = userService.getUserInfo(userDetails.getId());
        return new RsData<>("200", "회원정보 조회 성공",userInfoResponse);
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 이름, 학년, 학기, 전공을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = RsData.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 불일치)")
    @ApiResponse(responseCode = "404", description = "해당 전공을 찾을 수 없음")
    @ApiResponse(responseCode = "401", description = "인증 실패: 로그인이 필요합니다.")
    @PatchMapping("/infomation")
    public RsData<?> changeInfomation(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody ChangeInfoRequest request) {
        UserInfoResponse userInfoResponse = userService.changeInformation(userDetails.getId(),request);
        return new RsData<>("200", "회원정보 수정 성공",userInfoResponse);
    }
}
