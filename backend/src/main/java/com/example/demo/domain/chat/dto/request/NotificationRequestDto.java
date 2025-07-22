package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

//채팅방 알림 나중에 구현
public record NotificationRequestDto(
        UUID userId,
        String notificationToken
) {}
