package com.example.demo.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateChatRoomResponseDto(
        int code,
        String message,
        Data data
) {
    public record Data(UUID roomId, LocalDateTime createdAt) {}
}

