package com.example.demo.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메시지 읽음 처리 요청 DTO")
public record MarkMessagesReadRequestDto(
        @Schema(description = "채팅방 ID", example = "907c6d4f-...")
        UUID roomId,

        @Schema(description = "마지막으로 읽은 시간", example = "2025-10-15T12:00:00")
        LocalDateTime lastReadAt
) {
    public static MarkMessagesReadRequestDto of(UUID roomId, LocalDateTime lastReadAt) {
        return new MarkMessagesReadRequestDto(roomId, lastReadAt);
    }
}
