package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReceiveMessageResponseDto {
    private UUID messageId;
    private UUID roomId;
    private UUID senderId;
    private String message;
    private String imageUrl;
    private boolean isRead;
    private LocalDateTime sentAt;
}
