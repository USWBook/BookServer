package com.example.demo.global.jwt.exception;

import com.example.demo.global.exception.CustomJwtException;

public class JwtBlacklistedException extends CustomJwtException {
    public JwtBlacklistedException() {
        super("이미 로그아웃 처리된 토큰입니다.", "401");
    }
}
