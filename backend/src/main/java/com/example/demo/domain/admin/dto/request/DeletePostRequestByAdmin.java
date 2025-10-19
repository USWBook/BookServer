package com.example.demo.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeletePostRequestByAdmin(
        @NotNull(message = "게시물 ID는 필수입니다.")
        UUID postId
) {
}
