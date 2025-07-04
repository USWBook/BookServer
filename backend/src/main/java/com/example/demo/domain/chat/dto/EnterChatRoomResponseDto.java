package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class EnterChatRoomResponseDto {
    private int code;
    private String message;
    private Data data;

    @Getter
    @NoArgsConstructor
    public static class Data {
        private UUID roomId;
        private List<ReceiveMessageResponseDto> messages;
    }
}
