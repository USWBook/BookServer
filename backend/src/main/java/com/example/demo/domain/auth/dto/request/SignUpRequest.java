package com.example.demo.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        // 비밀번호 정규식 (영문, 숫자, 특수문자 포함 8~20자)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.")
        String password,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "학번은 필수입니다.")
        String studentId,

        @NotBlank(message = "전공은 필수입니다.")
        String majorName,

        @NotNull(message = "학년은 필수입니다.")
        Integer grade,

        @NotNull(message = "학기는 필수입니다.")
        Integer semester
) {}
