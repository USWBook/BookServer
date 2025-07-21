package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class InvalidChatArgumentException extends ChatException {
    public InvalidChatArgumentException() {
        super("올바르지 않은 채팅 요청입니다.", "400");
    }
}
