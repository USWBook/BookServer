package com.example.demo.domain.mail.dto;

import com.example.demo.domain.mail.enums.EmailAuthPurpose;
import com.example.demo.global.annotation.enums.Enum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 확인 요청 DTO")
public record MailVerificationDto(
        @Schema(description = "인증 이메일", example = "example@email.com)")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "인증번호", example = "123456")
        @NotBlank(message = "인증번호는 필수입니다.")
        String authCode,

        @Schema(description = "인증사유", example = "회원가입 :SIGN_UP, 비밀번호 초기화 :PASSWORD_RESET)")
        @Enum(enumClass = EmailAuthPurpose.class, message = "유효한 인증 목적이 아닙니다. (SIGN_UP, PASSWORD_RESET)")
        @NotBlank(message = "인증사유는 필수입니다.")
        String purpose
) {
}
