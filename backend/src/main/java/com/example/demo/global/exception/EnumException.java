package com.example.demo.global.exception;

import lombok.Getter;

public class EnumException extends RuntimeException {
    private final String code;
    public EnumException(String message, String code) {
        super(message);
        this.code = code;
    }
    public int getStatusCode() { return Integer.parseInt(code);}
}