package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class TokenMismatchException extends AuthException {
    public TokenMismatchException() {
        super("403", "토큰이 일치하지 않습니다.");
    }
}
