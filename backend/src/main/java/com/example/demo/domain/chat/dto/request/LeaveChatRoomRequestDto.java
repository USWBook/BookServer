package com.example.demo.domain.chat.dto;

import java.util.UUID;

public record LeaveChatRoomRequestDto(UUID roomId, UUID userId) {}
