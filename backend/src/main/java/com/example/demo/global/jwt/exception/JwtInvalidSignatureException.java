package com.example.demo.global.jwt.exception;

import com.example.demo.global.exception.CustomJwtException;

// 잘못된 서명
public class JwtInvalidSignatureException extends CustomJwtException {
    public JwtInvalidSignatureException() {
        super("토큰 서명이 유효하지 않습니다.", "401");
    }
}
