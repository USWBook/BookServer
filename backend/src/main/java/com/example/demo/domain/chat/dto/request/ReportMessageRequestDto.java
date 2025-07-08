package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record ReportMessageRequestDto(UUID messageId, UUID reporterId, String reason) {}
