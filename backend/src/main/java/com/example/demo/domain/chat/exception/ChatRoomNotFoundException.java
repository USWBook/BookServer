package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class ChatRoomNotFoundException extends ChatException {
    public ChatRoomNotFoundException() {
        super("존재하지 않는 채팅방입니다.", "404");
    }
}
