package com.example.demo.domain.user.controller;

import com.example.demo.domain.post.dto.response.PostResponse;
import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.dto.UploadPost;
import com.example.demo.domain.user.dto.UserWithdrawalRequest;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.annotation.swagger.ApiUnauthorizedResponse;
import com.example.demo.global.security.userdetails.CustomUserDetails;
import com.example.demo.domain.user.response.UserInfoResponse;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "User", description = "사용자 정보 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiSuccessResponse(description = "회원정보 조회 성공")
    @ApiErrorResponse(
            responseCode = "400",
            description = "예기치 못한 예외",
            exampleName = "UnknownFound",
            exampleValue = "{\"code\": \"400\", \"message\": \"예기치 못한 예외. 개발자 문의 바람\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "사용자를 차을 수 없음",
            exampleName = "UserNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"사용자를 찾을 수 없습니다\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "401",
            description = "인증 실패 (모든 토큰 이슈)",
            exampleName = "TokenExpired",
            exampleValue = "{\"code\": \"401\", \"message\": \"토큰이 만료되었습니다.\", \"data\": null}"
    )
    @ApiUnauthorizedResponse // 401
    @GetMapping("/information")
    public RsData<UserInfoResponse> infomation(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse userInfoResponse = userService.getUserInfo(userDetails.getId());
        return RsData.of("200", "회원정보 조회 성공", userInfoResponse);
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 이름, 학년, 학기, 전공을 수정합니다.")
    @ApiSuccessResponse(description = "회원정보 수정 성공")
    @ApiErrorResponse(
            description = "유효성 검사 실패",
            exampleName = "ValidationFailure",
            exampleValue = "{\"code\": \"400\", \"message\": \"학기 혹은 학년이 잘못 입력 되었습니다\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "401",
            description = "인증 실패 (모든 토큰 이슈)",
            exampleName = "TokenExpired",
            exampleValue = "{\"code\": \"401\", \"message\": \"토큰이 만료되었습니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "404",
            description = "찾을 수 없는 전공",
            exampleName = "MajorNotFound",
            exampleValue = "{\"code\": \"404\", \"message\": \"존재하지 않는 전공입니다.\", \"data\": null}"
    )
    @ApiUnauthorizedResponse
    @PatchMapping("/information")
    public RsData<UserInfoResponse> changeInfomation(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ChangeInfoRequest request) {
        UserInfoResponse userInfoResponse = userService.changeInformation(userDetails.getId(), request);
        return RsData.of("200", "회원정보 수정 성공", userInfoResponse);
    }

    @PostMapping("/withdrawal")
    public RsData<?> withdrawUserWithPassword(
            @RequestBody UserWithdrawalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.withdraw(userDetails.getId(), request.password());

        return RsData.of("200", "회원 탈퇴가 성공적으로 처리되었습니다.");

    }

    @GetMapping("/post")
    public RsData<Page<UploadPost>> getUploadPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<UploadPost> uploadPostsPage = userService.getMyPosts(userDetails.getId(), pageable);

        return RsData.of("200","내가 올린 게시물 조회 성공",uploadPostsPage);
    }

    @GetMapping("/likePost")
    public RsData<Page<UploadPost>> getLikePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        Page<UploadPost> likePostsPage = userService.getMyLikePosts(userDetails.getId(), pageable);

        return RsData.of("200","내가 찜한 게시물 조회 성공",likePostsPage);
    }

    @GetMapping("/purchases")
    public RsData<Page<PostResponse>> getPurchaseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        Page<PostResponse> purchasePage = userService.getMyPurchaseList(userDetails.getId(),pageable);

        return RsData.of("200","내가 구매한 게시물 조회 성공",purchasePage);
    }

}
