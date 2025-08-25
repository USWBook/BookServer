package com.example.demo.domain.mail.controller;

import com.example.demo.domain.mail.dto.MailRequestDto;
import com.example.demo.domain.mail.dto.MailVerificationDto;
import com.example.demo.domain.mail.service.MailService;
import com.example.demo.global.response.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    // 인증 이메일 발송
    @PostMapping("/send-verification")
    public RsData<?> sendVerificationCode(@RequestBody MailRequestDto requestDto) {
        mailService.sendVerificationCode(requestDto.email());
        return new RsData<>("200", "인증 코드가 성공적으로 발송되었습니다.");
    }

    // 이메일 인증 확인
    @PostMapping("/verify")
    public RsData<?> verifyEmail(@RequestBody MailVerificationDto verificationDto) {
        mailService.verifyEmail(verificationDto.email(), verificationDto.authCode());
        return new RsData<>("200", "이메일 인증이 성공적으로 완료되었습니다.");
    }

}