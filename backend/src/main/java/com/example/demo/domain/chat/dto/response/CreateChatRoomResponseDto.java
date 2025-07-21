package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateChatRoomResponseDto(
        // 응답 코드 (성공: 200 등)
        int code,

        // 응답 메시지
        String message,

        // 실제 응답 데이터
        Data data
) {
    // 실제 응답 데이터 (채팅방 ID, 생성시간 등 포함)
    public record Data(
            // 생성된 채팅방 고유 ID
            UUID roomId,

            // 채팅방 생성 시각
            LocalDateTime createdAt
    ) {}

    // ChatRoom으로부터 응답 DTO를 만들기 위한 정적 메서드
    public static CreateChatRoomResponseDto from(ChatRoom chatRoom) {
        return new CreateChatRoomResponseDto(
                200, // 성공 응답 코드
                "채팅방 생성 성공", // 응답 메시지
                new Data(
                        chatRoom.getRoomId(),    // 채팅방 고유 ID
                        chatRoom.getCreatedAt()  // 채팅방 생성 시각
                )
        );
    }
}