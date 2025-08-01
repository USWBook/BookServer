package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.UUID;

// CreateChatRoomResponseDto.java
public record CreateChatRoomResponseDto(
        UUID roomId,
        LocalDateTime createdAt
) {
    public static CreateChatRoomResponseDto from(ChatRoom chatRoom) {
        return new CreateChatRoomResponseDto(
                chatRoom.getRoomId(),
                chatRoom.getCreatedAt()
        );
    }
}
