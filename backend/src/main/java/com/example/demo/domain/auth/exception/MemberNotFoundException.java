package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class MemberNotFoundException extends AuthException {
    public MemberNotFoundException() {
        super("404", "회원이 존재하지 않습니다.");
    }
}