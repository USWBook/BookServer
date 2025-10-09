package com.example.demo.domain.post.dto.request;

import com.example.demo.domain.post.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "검색 필터 파라미터")
public class PostSearchCondition {
    @Schema(description = "학년", example = "4")
    private Integer grade;      // 학년 필터
    @Schema(description = "판매상태", example = "판매중")
    private PostStatus status;  // 판매 상태 필터
    @Schema(description = "책이름", example = "두근두근 자료구조")
    private String bookName;     // 책 이름
    @Schema(description = "강의명", example = "너도 자료구조 할 수 있어")
    private String className; // 강의명 
}
