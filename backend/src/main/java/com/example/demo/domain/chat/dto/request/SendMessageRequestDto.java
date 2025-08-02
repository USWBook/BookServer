package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record SendMessageRequestDto(
        UUID roomId,
        UUID senderId,
        String message
) {}
