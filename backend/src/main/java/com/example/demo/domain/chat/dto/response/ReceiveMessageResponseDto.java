package com.example.demo.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ReceiveMessageResponseDto(
        UUID myId,                       // 로그인한 내 ID
        List<Message> messages           // 메시지 배열
) {
    public record Message(
            UUID messageId,
            UUID roomId,
            UUID senderId,
            String message,
            String imageUrl,
            boolean isRead,
            LocalDateTime sentAt
    ) {}
}
