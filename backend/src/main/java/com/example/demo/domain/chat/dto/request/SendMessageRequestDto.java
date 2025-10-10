package com.example.demo.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "메시지 전송 요청 DTO")
public record SendMessageRequestDto(
        @Schema(description = "채팅방 ID", example = "907c6d4f-...")
        UUID roomId,

        @Schema(description = "전송할 메시지 내용", example = "안녕하세요")
        String message
) {}
