package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class LeaveChatRoomRequestDto {
    private UUID roomId;
    private UUID userId;
}
