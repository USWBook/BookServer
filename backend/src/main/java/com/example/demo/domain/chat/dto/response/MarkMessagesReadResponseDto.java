package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;
import java.time.LocalDateTime;

@Schema(description = "메시지 읽음 처리 응답 DTO")
public record MarkMessagesReadResponseDto(
        @Schema(description = "채팅방 ID", example = "907c6d4f-...")
        UUID roomId,

        @Schema(description = "마지막으로 읽은 시간", example = "2025-10-15T12:00:00")
        LocalDateTime lastReadAt,

        @Schema(description = "사용자 ID", example = "3fa85f64-...")
        UUID userId
) {}
