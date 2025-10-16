package com.example.demo.domain.mail.exception;

import com.example.demo.global.exception.AuthException;

public class InvalidEmailAuthPurposeException extends AuthException {
    public InvalidEmailAuthPurposeException(String email) {
        super(email + " 인증코드를 요청하지 않음" , "404" );
    }
}
