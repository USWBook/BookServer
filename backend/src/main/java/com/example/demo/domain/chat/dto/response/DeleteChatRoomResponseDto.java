package com.example.demo.domain.chat.dto.response;

//메시지 삭제 (x)
public record DeleteChatRoomResponseDto(
        int code,
        String message
) {}
