package com.example.demo.domain.user.exception;

import com.example.demo.global.exception.AuthException;

public class PasswordNotEqualException extends AuthException {
    public PasswordNotEqualException() {
        super("비밀번호가 틀렸습니다","403");
    }
}
