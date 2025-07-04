package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequestDto {
    private UUID postId;
    private UUID sellerId;
}

