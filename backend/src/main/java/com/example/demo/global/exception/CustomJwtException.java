package com.example.demo.global.exception;

import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException {
    private final String code;

    public CustomJwtException(String message, String code) {
        super(message);
        this.code = code;
    }
    public int getStatusCode() { return Integer.parseInt(code);}
}
