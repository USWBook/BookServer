package com.example.demo.domain.mail.dto;

import com.example.demo.global.annotation.enums.Enum;
import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이메일 인증코드 전송 요청 DTO")
public record MailRequestOrVerifyDto(
        @Schema(description = "전송받을 이메일", example = "example@email.com")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @NotNull(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "인증사유", example = "회원가입 :SIGN_UP, 비밀번호 초기화 :PASSWORD_RESET)")
        @NotNull(message = "인증 목적은 필수입니다.")
        EmailAuthPurpose purpose
) {
}
