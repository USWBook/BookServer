package com.example.demo.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 DTO")
public record LoginRequest(
        @Schema(description = "이메일", example = "example@suwon.ac.kr")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "비밀번호", example = "@!a12345")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {}
