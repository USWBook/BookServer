package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class ChatAlreadyDeletedException extends ChatException {
    public ChatAlreadyDeletedException() {
        super("이미 삭제된 채팅방 혹은 메시지입니다.", "410");
    }
}