package com.example.demo.domain.chat.dto;

import java.util.UUID;

public record CreateChatRoomRequestDto(UUID postId, UUID sellerId) {}
