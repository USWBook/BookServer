package com.example.demo.domain.chat.exception;

import com.example.demo.global.exception.ChatException;

public class ChatImageUploadException extends ChatException {
    public ChatImageUploadException() {
        super("이미지 업로드 중 오류가 발생했습니다.", "500");
    }
}
