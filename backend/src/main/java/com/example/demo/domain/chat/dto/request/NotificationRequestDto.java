package com.example.demo.domain.chat.dto.request;

import java.util.UUID;

public record NotificationRequestDto(UUID userId, String notificationToken) {}
