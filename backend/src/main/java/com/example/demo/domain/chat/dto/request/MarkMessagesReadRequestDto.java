package com.example.demo.domain.chat.dto;

import java.util.List;
import java.util.UUID;

public record MarkMessagesReadRequestDto(UUID roomId, UUID userId, List<UUID> messageIds) {}
