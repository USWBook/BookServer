package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.dto.CustomUserDetails;
import com.example.demo.domain.user.response.UserInfoResponse;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/infomation")
    public RsData<?> infomation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UserInfoResponse userInfoResponse = userService.getUserInfo(userDetails.getId());
        return new RsData<>("200", "회원정보 조회 성공",userInfoResponse);
    }

    @PatchMapping("/infomation")
    public RsData<?> changeInfomation(@AuthenticationPrincipal CustomUserDetails userDetails,@RequestBody ChangeInfoRequest request) {
        UserInfoResponse userInfoResponse = userService.changeInformation(userDetails.getId(),request);
        return new RsData<>("200", "회원정보 수정 성공",userInfoResponse);
    }
}
