package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record CreateChatRoomRequestDto(
        // 게시글 ID
        UUID postId,

        //판매자의 회원 ID
        UUID sellerId,

        //구매자(상대방)의 회원 ID
        UUID buyerId

) {}