package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class ArealyExistsName extends AuthException {
    public ArealyExistsName(String message) {
        super(message + "는 이미 사용중인 닉네임입니다.","409");
    }
}
