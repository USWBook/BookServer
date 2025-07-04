package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendMessageRequestDto {
    private UUID roomId;
    private UUID senderId;
    private String message;
}
