package com.example.demo.domain.mail.exception;

import com.example.demo.global.exception.AuthException;

public class InvalidOrExpiredVerificationCodeException extends AuthException {
    public InvalidOrExpiredVerificationCodeException() {
        super("인증 코드가 틀리거나 만료되었습니다.","401");
    }
}
