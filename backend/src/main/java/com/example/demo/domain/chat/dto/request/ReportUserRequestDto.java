package com.example.demo.domain.chat.dto.request;

import com.example.demo.domain.report.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "유저 신고 요청 DTO")
public record ReportUserRequestDto(
        @Schema(description = "신고 사유", example = "욕설")
        ReportReason reason
) {}
