package com.example.demo.domain.member.exception;

import com.example.demo.global.exception.BookException;

public class MemberNotFoundException extends BookException {
    public MemberNotFoundException() {
        super("404", "판매자 회원이 존재하지 않습니다.");
    }
}