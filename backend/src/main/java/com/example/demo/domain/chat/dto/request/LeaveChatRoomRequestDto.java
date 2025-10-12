package com.example.demo.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "채팅방 퇴장 요청 DTO")
public record LeaveChatRoomRequestDto(
        //채팅방 ID
        @Schema(description = "채팅방 ID", example = "af3d1234-...")
        UUID roomId
) {}
