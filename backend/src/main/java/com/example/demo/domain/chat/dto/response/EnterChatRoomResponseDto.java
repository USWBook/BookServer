package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.dto.response.ReceiveMessageResponseDto;

import java.util.List;
import java.util.UUID;

public record EnterChatRoomResponseDto(int code, String message, Data data) {
    public record Data(UUID roomId, List<ReceiveMessageResponseDto> messages) {}
}
