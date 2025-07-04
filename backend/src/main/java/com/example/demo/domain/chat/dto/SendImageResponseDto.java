package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class SendImageResponseDto {
    private int code;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private UUID messageId;
        private String imageUrl;
        private LocalDateTime sentAt;
    }
}
