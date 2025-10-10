package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "메시지 수신 응답 DTO")
public record ReceiveMessageResponseDto(
        @Schema(description = "로그인한 내 ID", example = "907c6d4f-...")
        UUID myId,

        @Schema(description = "메시지 배열")
        List<Message> messages
) {
    @Schema(description = "메시지 데이터")
    public record Message(
            @Schema(description = "메시지 ID", example = "1e8c6c44-...")
            UUID messageId,

            @Schema(description = "채팅방 ID", example = "907c6d4f-...")
            UUID roomId,

            @Schema(description = "보낸 사람 ID", example = "27b1f71b-...")
            UUID senderId,

            @Schema(description = "메시지 내용", example = "안녕하세요")
            String message,

            @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
            String imageUrl,

            @Schema(description = "읽음 여부", example = "true")
            boolean isRead,

            @Schema(description = "메시지 전송 시간", example = "2025-10-10T01:00:00")
            LocalDateTime sentAt
    ) {}
}
