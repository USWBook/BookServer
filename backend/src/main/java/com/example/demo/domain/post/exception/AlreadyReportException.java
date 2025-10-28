package com.example.demo.domain.post.exception;

import com.example.demo.global.exception.BookException;

public class AlreadyReportException  extends BookException {
    public AlreadyReportException() {super("400","이미 신고한 게시물입니다.");}
}
