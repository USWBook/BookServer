package com.example.demo.domain.user.dto;


import com.example.demo.domain.user.enums.Grade;
import com.example.demo.domain.user.enums.Semester;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;
import com.example.demo.global.annotation.enums.Enum;

@Schema(description = "사용자 정보 변경 DTO")
public record ChangeInfoRequest(
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "전공 식별값", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID majorId,

        @Schema(description = "학년", example = "4")
        @Enum(enumClass = Grade.class, message = "유효하지 않은 학년입니다.")
        Grade grade,

        @Schema(description = "학기", example = "2")
        @Enum(enumClass = Semester.class, message = "유효하지 않은 학기입니다.")
        Semester semester
) {
}
