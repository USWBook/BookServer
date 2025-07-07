package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record LeaveChatRoomRequestDto(UUID roomId, UUID userId) {}
