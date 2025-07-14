package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class InvalidTokenException extends AuthException {
    public InvalidTokenException() {
        super("401", "유효하지 않은 토큰입니다.");
    }
}

