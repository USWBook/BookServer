package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class MarkMessagesReadRequestDto {
    private UUID roomId;
    private UUID userId;
    private List<UUID> messageIds;
}
