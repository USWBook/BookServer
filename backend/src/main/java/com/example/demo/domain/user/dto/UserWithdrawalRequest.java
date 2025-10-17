package com.example.demo.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원탈퇴 요청 DTO")
public record UserWithdrawalRequest(

        @Schema(description = "비밀번호", example = "!@asdasd2")
        String password
) {
}
