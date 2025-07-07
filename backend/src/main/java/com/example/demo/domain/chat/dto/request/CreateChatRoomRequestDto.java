package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record CreateChatRoomRequestDto(UUID postId, UUID sellerId) {}
