package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "메시지 전송 응답 DTO")
public record SendMessageResponseDto(
        @Schema(description = "응답 코드", example = "200")
        int code,

        @Schema(description = "응답 메시지", example = "메시지 전송 성공")
        String message,

        @Schema(description = "응답 데이터")
        Data data
) {
    @Schema(description = "메시지 상세 데이터")
    public record Data(
            @Schema(description = "메시지 ID", example = "1e8c6c44-...")
            UUID messageId,

            @Schema(description = "채팅방 ID", example = "907c6d4f-...")
            UUID roomId,

            @Schema(description = "보낸 사람 ID", example = "27b1f71b-...")
            UUID senderId,

            @Schema(description = "메시지 내용", example = "안녕하세요")
            String message,

            @Schema(description = "메시지 전송 시간", example = "2025-10-10T01:00:00")
            LocalDateTime sentAt
    ) {}
}
