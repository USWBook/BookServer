package com.example.demo.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SendImageResponseDto(int code, String message, Data data) {
    public record Data(UUID messageId, String imageUrl, LocalDateTime sentAt) {}
}
