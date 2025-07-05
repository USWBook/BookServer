package com.example.demo.domain.post.exception;

import com.example.demo.global.exception.BookException;

public class PostNotFoundException extends BookException {
    public PostNotFoundException() {
        super("404", "존재하지 않는 게시글입니다.");
    }
}
