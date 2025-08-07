package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record CreateChatRoomRequestDto(
        // 게시글 ID
        UUID postId
) {}
