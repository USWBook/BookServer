package com.example.demo.domain.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarkMessagesReadResponseDto {
    private int code;
    private String message;
}
