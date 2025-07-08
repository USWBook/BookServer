package com.example.demo.domain.chat.dto.response;

import java.util.List;

public record ListChatRoomsResponseDto(int code, String message, List<ChatRoomDto> data) {
    public record ChatRoomDto(
            String roomId,
            String postId,
            String name,
            int userCount,
            String lastMessage,
            String lastTimestamp
    ) {}
}
