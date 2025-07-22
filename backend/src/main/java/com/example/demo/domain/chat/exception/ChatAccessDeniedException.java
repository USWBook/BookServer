package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class ChatAccessDeniedException extends ChatException {
    public ChatAccessDeniedException() {
        super("해당 채팅방에 접근 권한이 없습니다.", "403");
    }
}
