package com.example.demo.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

@Schema(description = "회원정보 DTO")
public record SignUpRequest(
        @Schema(description = "이메일", example = "example@suwon.ac.kr")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호", example = "@!a12345")
        @NotBlank(message = "비밀번호는 필수입니다.")
        // 비밀번호 정규식 (영문, 숫자, 특수문자 포함 8~20자)
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.")
        String password,

        @Schema(description = "닉네임", example = "호랑이")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "학번", example = "12345678")
        @NotBlank(message = "학번은 필수입니다.")
        String studentId,

        @Schema(description = "전공 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull(message = "전공은 필수입니다.")
        UUID majorId,

        @Schema(description = "학년", example = "4")
        @NotNull(message = "학년은 필수입니다.")
        Integer grade,

        @Schema(description = "학기", example = "2")
        @NotNull(message = "학기는 필수입니다.")
        Integer semester,

        @Schema(description = "프로필 이미지 URL (nullable)", example = "https://s3.ap-northeast-2.amazonaws.com/bucket/users/abc.jpg")
        String profileImageUrl
) {}
