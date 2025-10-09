package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.report.enums.ReportReason;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "유저 신고 응답 DTO")
public record ReportUserResponseDto(
        @Schema(description = "신고 ID", example = "907c6d4f-...")
        UUID reportId,

        @Schema(description = "채팅방 ID", example = "2e7d8db4-...")
        UUID roomId,

        @Schema(description = "신고자 유저 ID", example = "f3e3bc8b-...")
        UUID reportUserId,

        @Schema(description = "피신고자 유저 ID", example = "b30a2eac-...")
        UUID reportedUserId,

        @Schema(description = "신고 사유", example = "욕설")
        ReportReason reason,

        @Schema(description = "신고 시각", example = "2025-10-10T01:00:00")
        LocalDateTime reportedAt
) {}
