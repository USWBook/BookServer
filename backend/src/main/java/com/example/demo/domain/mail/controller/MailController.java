package com.example.demo.domain.mail.controller;

import com.example.demo.domain.mail.service.MailService;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Mail", description = "이메일 인증 API")
@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @Operation(summary = "인증코드 이메일 발송", description = "회원가입 또는 비밀번호 재설정을 위해 이메일로 인증 코드를 발송합니다.")
    @ApiSuccessResponse(description = "인증 코드 발송요청 성공했습니다.")
    @ApiErrorResponse(
            responseCode = "400",
            description = "gmail api 쪽  실패",
            exampleName = "APIFailure",
            exampleValue = "{\"code\": \"400\", " +
                    "\"message\": \"비동기 처리해서 요청이 들어오면 외부 api를 호출할 스레드를 할당한뒤 프론트에 바로 응답을 보내기에 외부 api에서 터진 예외를 프론트에 보낼 방법이 없으나 스웨거에 자꾸 400에러가 뜨는 이슈가 있어서 임시로 박아둠. 원래는 못잡는 예외임\"" +
                    ", \"data\": null}"
    )
    @PostMapping("/email-verifications")
    public RsData<?> sendVerificationCode(@RequestParam("email") String email) {
        mailService.sendVerificationCode(email);
        return RsData.of("202", "인증 코드 발송요청 성공했습니다.");
    }

    @Operation(summary = "인증코드 확인", description = "발송된 인증 코드를 사용하여 이메일 주소의 소유권을 확인합니다.")
    @ApiSuccessResponse(description = "이메일 인증이 성공적으로 완료되었습니다.")
    @ApiErrorResponse(
            responseCode = "401",
            description = "인증 실패 (잘못된 코드 또는 만료)",
            exampleName = "InvalidVerificationCode",
            exampleValue = "{\"code\": \"401\", \"message\": \"인증 코드가 틀리거나 만료되었습니다.\", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "400", // 기본이 400이라 생략 가능
            description = "인증 실패 (인증코드를 요청하지 않음)",
            exampleName = "NotRequestedVerificationCode",
            exampleValue = "{\"code\": \"400\", \"message\": \"인증코드를 요청하지 않음.\", \"data\": null}"
    )
    @GetMapping("/email-verifications")
    public RsData<?> verifyEmail(@RequestParam("email") String email,
                                 @RequestParam("authCode") String authCode) {
        mailService.verifyEmail(email, authCode);
        return RsData.of("200", "이메일 인증이 성공적으로 완료되었습니다.");
    }
}
