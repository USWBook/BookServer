package com.example.demo.domain.user.controller;

import com.example.demo.domain.auth.dto.request.SignUpRequest;
import com.example.demo.domain.auth.service.AuthService;
import com.example.demo.global.response.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final AuthService authService;

    @GetMapping("/infomation")
    public RsData<?> infomation(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 성공");
    }

    @PostMapping("/change-infomation")
    public RsData<?> changeInfomation(@RequestBody @Valid SignUpRequest request) {
        authService.signUp(request);
        return new RsData<>("200", "회원가입 성공");
    }
}
