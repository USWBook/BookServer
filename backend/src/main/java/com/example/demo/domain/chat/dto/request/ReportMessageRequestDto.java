package com.example.demo.domain.chat.dto;

import java.util.UUID;

public record ReportMessageRequestDto(UUID messageId, UUID reporterId, String reason) {}
