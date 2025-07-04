package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class NotificationRequestDto {
    private UUID userId;
    private String notificationToken;
}
