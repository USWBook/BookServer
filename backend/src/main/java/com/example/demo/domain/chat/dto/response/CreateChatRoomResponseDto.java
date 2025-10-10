package com.example.demo.domain.chat.dto.response;

import com.example.demo.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "채팅방 생성 응답 DTO")
public record CreateChatRoomResponseDto(
        @Schema(description = "채팅방 ID", example = "907c6d4f-...")
        UUID roomId,

        @Schema(description = "채팅방 생성 시간", example = "2025-10-10T01:00:00")
        LocalDateTime createdAt,

        @Schema(description = "채팅방 제목", example = "중고책 거래방")
        String title

        //프로필 추가하면 주석 풀기
        //@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        //String profileImage
) {
    public static CreateChatRoomResponseDto from(ChatRoom chatRoom,String title) {
        return new CreateChatRoomResponseDto(
                chatRoom.getRoomId(),
                chatRoom.getCreatedAt(),
                title
                //profileImage
        );
    }
}
