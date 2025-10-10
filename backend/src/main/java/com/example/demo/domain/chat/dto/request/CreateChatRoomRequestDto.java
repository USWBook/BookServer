package com.example.demo.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "채팅방 생성 요청 DTO")
public record CreateChatRoomRequestDto(
        // 게시글 ID
        @Schema(description = "채팅 대상 게시글 ID", example = "907c6d4f-...")
        UUID postId
) {}
