package com.example.demo.domain.chat.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

//이미지 전송
public record SendImageResponseDto(
        int code,
        String message,
        Data data
) {
    public record Data(
            UUID messageId,
            UUID roomId,      //
            UUID senderId,    //
            String imageUrl,
            LocalDateTime sentAt
    ) {}
}
