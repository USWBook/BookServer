package com.example.demo.global.exception;

import lombok.Getter;

public abstract class ChatException extends RuntimeException {

    @Getter
    private final String code;

    public ChatException(String message, String statusCode) {
        super(message);
        this.code = statusCode;
    }

    public int getStatusCode() { return Integer.parseInt(code);}
}