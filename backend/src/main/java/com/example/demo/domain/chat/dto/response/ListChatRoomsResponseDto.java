package com.example.demo.domain.chat.dto.response;

import java.util.List;

//채팅방 목록 조회
public record ListChatRoomsResponseDto(List<ChatRoomDto> data) {
    public record ChatRoomDto(
            String roomId,
            String postId,
            String name,
            int userCount,
            String lastMessage,
            String lastTimestamp
    ) {}
}
