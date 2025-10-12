package com.example.demo.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 입장 응답 DTO")
public record EnterChatRoomResponseDto(
        @Schema(description = "응답 코드", example = "200")
        int code,

        @Schema(description = "응답 메시지", example = "채팅방 입장 성공")
        String message,

        @Schema(description = "데이터 객체")
        Data data
) {
    @Schema(description = "채팅방 입장 상세 데이터")
    public record Data(
            @Schema(description = "채팅방 ID", example = "907c6d4f-...")
            String roomId,

            @Schema(description = "게시글 ID", example = "2f1b8a4e-...")
            String postId,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name,

            @Schema(description = "채팅방 현재 인원 수", example = "2")
            int userCount,

            @Schema(description = "마지막 메시지 내용", example = "안녕하세요")
            String lastMessage,

            @Schema(description = "마지막 메시지 전송 시간", example = "2025-10-10T01:00:00")
            String lastTimestamp
    ) {}
}
