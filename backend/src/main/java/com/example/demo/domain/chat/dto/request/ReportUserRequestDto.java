package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record ReportUserRequestDto(UUID reportedUserId, UUID reporterId, String reason) {}
