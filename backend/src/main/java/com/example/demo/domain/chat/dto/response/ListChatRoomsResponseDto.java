package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "채팅방 목록 조회 응답 DTO")
public record ListChatRoomsResponseDto(
        @Schema(description = "채팅방 목록 데이터")
        List<ChatRoomDto> data
) {
    @Schema(description = "채팅방 정보 DTO")
    public record ChatRoomDto(
            @Schema(description = "채팅방 ID", example = "907c6d4f-...")
            String roomId,

            @Schema(description = "게시글 ID", example = "2f1b8a4e-...")
            String postId,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name,

            @Schema(description = "게시글 제목", example = "자료구조 팝니다")
            String postName,

            @Schema(description = "채팅방 현재 인원 수", example = "2")
            int userCount,

            @Schema(description = "마지막 메시지 내용", example = "안녕하세요!")
            String lastMessage,

            @Schema(description = "마지막 메시지 전송 시간", example = "2025-10-10T01:00:00")
            String lastTimestamp
    ) {}
}
