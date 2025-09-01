package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.ChangeInfoRequest;
import com.example.demo.domain.user.response.UserInfoResponse;
import com.example.demo.domain.user.service.UserService;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/infomation")
    public RsData<?> infomation(@RequestHeader(value = "Authorization") String authHeader) {
        UserInfoResponse userInfoResponse = userService.getUserInfo(authHeader);
        return new RsData<>("200", "회원정보 조회 성공",userInfoResponse);
    }

    @PostMapping("/change-infomation")
    public RsData<?> changeInfomation(@RequestBody ChangeInfoRequest request) {
        userService.changeInformation(request);
        return new RsData<>("200", "회원정보 수정 성공");
    }
}
