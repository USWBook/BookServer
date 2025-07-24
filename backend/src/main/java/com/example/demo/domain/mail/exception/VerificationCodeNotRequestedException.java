package com.example.demo.domain.mail.exception;

import com.example.demo.global.exception.AuthException;

public class VerificationCodeNotRequestedException extends AuthException {
    public VerificationCodeNotRequestedException() {
        super("인증코드를 요청하지 않음" , "400" );
    }
}
