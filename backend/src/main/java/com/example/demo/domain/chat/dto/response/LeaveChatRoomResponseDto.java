package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 퇴장 응답 DTO")
public record LeaveChatRoomResponseDto(
        @Schema(description = "남아있는 사용자 수", example = "1")
        int userCount // 남아있는 사용자 수
) {}
