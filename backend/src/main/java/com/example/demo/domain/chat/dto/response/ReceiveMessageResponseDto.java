package com.example.demo.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiveMessageResponseDto(
        UUID messageId,
        UUID roomId,
        UUID senderId,
        String message,
        String imageUrl,
        boolean isRead,
        LocalDateTime sentAt
) {}
