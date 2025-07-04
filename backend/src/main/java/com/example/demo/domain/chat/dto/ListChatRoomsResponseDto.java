package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ListChatRoomsResponseDto {
    private int code;
    private String message;
    private List<ChatRoomDto> data;

    @Getter
    @NoArgsConstructor
    public static class ChatRoomDto {
        private String roomId;
        private String postId;
        private String name;
        private int userCount;
        private String lastMessage;
        private String lastTimestamp;
    }
}
