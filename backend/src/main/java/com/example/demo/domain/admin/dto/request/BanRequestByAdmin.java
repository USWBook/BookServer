package com.example.demo.domain.admin.dto.request;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BanRequestByAdmin(
        @NotNull(message = "밴할 사용자의 식별값은 필수입니다.")
        UUID userId
) {
}
