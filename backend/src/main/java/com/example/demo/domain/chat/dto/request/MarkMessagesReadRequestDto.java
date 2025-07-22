package com.example.demo.domain.chat.dto.request;

import java.util.List;
import java.util.UUID;

//메시지 읽음처리 나중에 구현
public record MarkMessagesReadRequestDto(
        UUID roomId,
        UUID userId,
        List<UUID> messageIds
) {}
