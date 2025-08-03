package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.dto.response.ReceiveMessageResponseDto;

import java.util.List;
import java.util.UUID;

//채팅방 입장
public record EnterChatRoomResponseDto(int code, String message, Data data) {
    public record Data(
            String roomId,
            String postId,
            String name,
            int userCount,
            String lastMessage,
            String lastTimestamp
    ) {}
}
