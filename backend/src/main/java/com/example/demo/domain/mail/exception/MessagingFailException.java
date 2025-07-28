package com.example.demo.domain.mail.exception;

import com.example.demo.global.exception.AuthException;

public class MessagingFailException extends AuthException {
    public MessagingFailException(String message) {
        super(message, "500");
    }
}
