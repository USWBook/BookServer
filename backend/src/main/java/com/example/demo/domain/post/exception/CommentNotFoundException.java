package com.example.demo.domain.post.exception;

import com.example.demo.global.exception.BookException;

public class CommentNotFoundException extends BookException {
    public CommentNotFoundException() {
        super("404", "존재하지 않는 댓글입니다.");
    }
}