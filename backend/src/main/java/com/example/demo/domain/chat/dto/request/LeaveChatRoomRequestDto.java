package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record LeaveChatRoomRequestDto(
        //채팅방 ID
        UUID roomId,
        //나가려는 사용자 ID
        UUID userId
) {}
