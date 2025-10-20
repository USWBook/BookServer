package com.example.demo.domain.admin.dto.request;


import jakarta.validation.constraints.NotNull;

public record BanRequestByAdmin(
        @NotNull(message = "밴할 사용자의 닉네임은 필수입니다.")
        String userName
) {
}
