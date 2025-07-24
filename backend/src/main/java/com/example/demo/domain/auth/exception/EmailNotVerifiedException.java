package com.example.demo.domain.auth.exception;

import com.example.demo.global.exception.AuthException;

public class EmailNotVerifiedException extends AuthException {
    public EmailNotVerifiedException() {
        super("이메일 인증을 완료 하십시오.","403");
    }
}
