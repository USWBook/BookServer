package com.example.demo.domain.mail.controller;

import com.example.demo.domain.mail.dto.MailRequestDto;
import com.example.demo.domain.mail.dto.MailVerificationDto;
import com.example.demo.domain.mail.service.MailService;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mail", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @Operation(summary = "인증 이메일 발송", description = "회원가입 또는 비밀번호 재설정을 위해 이메일로 인증 코드를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "인증 코드 발송 성공")
    @PostMapping("/send-verification")
    public RsData<?> sendVerificationCode(@RequestBody MailRequestDto requestDto) {
        mailService.sendVerificationCode(requestDto.email());
        return new RsData<>("200", "인증 코드가 성공적으로 발송되었습니다.");
    }

    @Operation(summary = "이메일 인증 확인", description = "발송된 인증 코드를 사용하여 이메일 주소의 소유권을 확인합니다.")
    @ApiResponse(responseCode = "200", description = "이메일 인증 성공")
    @ApiResponse(responseCode = "400", description = "인증 실패 (잘못된 코드 또는 만료)")
    @ApiResponse(responseCode = "400", description = "인증 실패 (인증코드를 요청하지 않음)")
    @PostMapping("/verify")
    public RsData<?> verifyEmail(@RequestBody MailVerificationDto verificationDto) {
        mailService.verifyEmail(verificationDto.email(), verificationDto.authCode());
        return new RsData<>("200", "이메일 인증이 성공적으로 완료되었습니다.");
    }
}
