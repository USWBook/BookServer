package com.example.demo.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SendMessageResponseDto(int code, String message, Data data) {
    public record Data(UUID messageId, UUID roomId, UUID senderId, String message, LocalDateTime sentAt) {}
}
