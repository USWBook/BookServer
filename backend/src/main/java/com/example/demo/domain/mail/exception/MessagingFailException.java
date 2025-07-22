package com.example.demo.domain.mail.exception;

import com.example.demo.global.exception.AuthException;

public class MessagingFailException extends AuthException {
    public MessagingFailException() {
        super("이메일 발송에 실패했습니다", "401");
    }
}
