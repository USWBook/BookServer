package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super("존재하지 않는 사용자입니다.","404");
    }
}
