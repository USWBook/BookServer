package com.example.demo.domain.chat.dto;

import java.util.UUID;

public record NotificationRequestDto(UUID userId, String notificationToken) {}
