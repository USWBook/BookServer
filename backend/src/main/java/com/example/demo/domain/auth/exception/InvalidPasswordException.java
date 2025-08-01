package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class InvalidPasswordException extends AuthException {
    public InvalidPasswordException() {
        super( "비밀번호가 잘못되었습니다.","400");
    }
}

