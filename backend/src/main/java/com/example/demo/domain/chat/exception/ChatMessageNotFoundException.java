package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class ChatMessageNotFoundException extends ChatException {
    public ChatMessageNotFoundException() {
        super("존재하지 않는 메시지입니다.", "404");
    }
}
