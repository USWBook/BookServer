package com.example.demo.domain.mail.controller;

import com.example.demo.domain.mail.dto.MailRequestOrVerifyDto;
import com.example.demo.domain.mail.dto.MailStatusResponse;
import com.example.demo.domain.mail.dto.MailVerificationDto;
import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.domain.mail.enums.MailStatus;
import com.example.demo.domain.mail.exception.InvalidEmailAuthPurposeException;
import com.example.demo.domain.mail.service.MailService;
import com.example.demo.global.annotation.swagger.ApiErrorResponse;
import com.example.demo.global.annotation.swagger.ApiSuccessResponse;
import com.example.demo.global.response.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
            responseCode = "500",
            description = "터지면 잡을 예정(아직 다른게 급해가)",
            exampleName = "APIFailure",
            exampleValue = "{\"code\": \"500\", " +
                    "\"message\": \"(예: Redis 연결 실패 등)\"" +
                    ", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "429",
            description = "1분내로 재요청 보낼경우",
            exampleName = "TooManyMailRequest",
            exampleValue = "{\"code\": \"429\", \"message\": \"인증 메일 요청은 1분에 한 번만 가능합니다.\", \"data\": null}"
    )
    @PostMapping("/email-verifications")
    public RsData<?> sendVerificationCode(@RequestBody @Valid MailRequestOrVerifyDto mailRequestOrVerifyDto) {
        try {
            mailService.sendVerificationCode(mailRequestOrVerifyDto);
            return RsData.of("202", "인증 코드 발송요청 성공했습니다.");
        } catch (Exception e) {
            // MailService에서 동기적으로 발생할 수 있는 예외 처리
            // (예: Redis 연결 실패 등)
            return RsData.of("500", "인증 코드 발송 요청에 실패했습니다: " + e.getMessage());
        }
    }
    @Operation(summary = "인증코드 이메일 발송 상태 확인", description = "이메일 전송은 비동기 스레드에서 처리하기에 상태추적하기위함")
    @ApiSuccessResponse(description = "메일 발송 상태 조회. 전송요청 후 202 응답 받았으면 \n2초뒤 한번 1초뒤 한번 1초뒤 한번 이렇게 네번정도 요청하면 될듯\n" +
            " PENDING:처리 중,\n" +
            "    SUCCESS:성공\n," +
            "    FAILED:실패"
            ,message = "메일 발송 상태 조회 성공.", dataType = MailStatusResponse.class)
    @ApiErrorResponse(
            responseCode = "404",
            description = "인증요청 한적 없음",
            exampleName = "NotSendEmail",
            exampleValue = "{\"code\": \"404\", " +
                    "\"message\": \"email + \" 인증코드를 요청하지 않음\"\"" +
                    ", \"data\": null}"
    )
    @ApiErrorResponse(
            responseCode = "400-1",
            description = "이넘에 맞지 않은 파라미터가 들어옴",
            exampleName = "EnumValidateFail",
            exampleValue = "{\"code\": \"404\", " +
                    "\"message\": 'purpose' 필드에 유효하지 않은 형식의 값이 입력되었습니다.\"\n" +
                    "\"" +
                    ", \"data\": null}"
    )
    @GetMapping("/status")
    public RsData<MailStatusResponse> getMailStatus(@ModelAttribute @Valid MailRequestOrVerifyDto mailRequestOrVerifyDto) {
        MailStatus status = mailService
                .getMailStatus(mailRequestOrVerifyDto.email(),mailRequestOrVerifyDto.purpose())
                .orElseThrow(() -> new InvalidEmailAuthPurposeException(mailRequestOrVerifyDto.email()));

        return RsData.of("200", "메일 발송 상태 조회 성공", new MailStatusResponse(status.name()));
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
    public RsData<?> verifyEmail(@ModelAttribute @Valid MailVerificationDto mailVerificationDto) {
        mailService.verifyEmail(mailVerificationDto.email(), mailVerificationDto.authCode(),EmailAuthPurpose.valueOf(mailVerificationDto.purpose()));
        return RsData.of("200", "이메일 인증이 성공적으로 완료되었습니다.");
    }
}
