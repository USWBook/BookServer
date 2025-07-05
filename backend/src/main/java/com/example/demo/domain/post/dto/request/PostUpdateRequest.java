package com.example.demo.domain.post.dto.request;

// 게시글 수정 요청 DTO
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    private String title;
    private String content;
    private Integer postPrice;
}
