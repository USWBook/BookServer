package com.example.demo.domain.post.exception;

import com.example.demo.global.exception.BookException;

public class CommentNotInPostException extends BookException {
    public CommentNotInPostException() {
        super("409", "댓글이 요청 준 게시물의 댓글이 아닙니다.");
    }
}