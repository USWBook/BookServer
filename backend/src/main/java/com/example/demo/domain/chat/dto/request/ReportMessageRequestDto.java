package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

//신고 기능 나중에 구현
public record ReportMessageRequestDto(
        UUID messageId,
        UUID reporterId,
        String reason
) {}
