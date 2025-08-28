package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.report.enums.ReportReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportUserResponseDto(
        UUID reportId,
        UUID roomId,
        UUID reportUserId,
        UUID reportedUserId,
        ReportReason reason,
        LocalDateTime reportedAt
) {}
