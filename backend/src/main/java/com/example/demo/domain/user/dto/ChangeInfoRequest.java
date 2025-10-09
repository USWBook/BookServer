package com.example.demo.domain.user.dto;


import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "사용자 정보 변경 DTO")
public record ChangeInfoRequest(
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,
        @Schema(description = "전공 식별값", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID majorId,
        @Schema(description = "학년", example = "4")
        Integer grade,
        @Schema(description = "학기", example = "2")
        Integer semester
) {
}
