package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.post.enums.PostStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchCondition {
    private Integer grade;      // 학년 필터
    private PostStatus status;  // 판매 상태 필터
    private String bookName;     // 책 이름
    private String className; // 강의명 
}
