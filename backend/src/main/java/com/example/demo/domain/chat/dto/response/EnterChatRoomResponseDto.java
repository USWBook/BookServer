package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.dto.response.ReceiveMessageResponseDto;

import java.util.List;
import java.util.UUID;

//채팅방 입장
public record EnterChatRoomResponseDto(int code, String message, Data data) {

    public record Data(
            UUID roomId,
            ParticipantDto opponent,  // ✅ 상대방 정보
            List<ReceiveMessageResponseDto> messages
    ) {}

    public record ParticipantDto(
            UUID userId,
            String nickname,
            String profileImageUrl
    ) {}
}
