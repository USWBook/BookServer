package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.UUID;

// CreateChatRoomResponseDto.java
public record CreateChatRoomResponseDto(
        UUID roomId,
        LocalDateTime createdAt,
        String title
        //프로필 추가하면 주석 풀기
        //String profileImage
) {
    public static CreateChatRoomResponseDto from(ChatRoom chatRoom,String title) {
        return new CreateChatRoomResponseDto(
                chatRoom.getRoomId(),
                chatRoom.getCreatedAt(),
                title
                //profileImage
        );
    }
}
