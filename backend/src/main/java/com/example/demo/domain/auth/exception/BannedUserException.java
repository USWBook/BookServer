package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class BannedUserException extends AuthException {
    public BannedUserException() {
        super("밴 먹은 유저입니다","403");
    }
}
