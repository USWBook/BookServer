package com.example.demo.domain.post.dto.request;
// 게시글 생성 요청 DTO
import lombok.Getter;

@Getter
public class PostCreateRequest {
    private String title;
    private String postName;
    private Integer postPrice;
    private String professor;
    private String courseName;
    private Integer grade;
    private Integer semester;
    private String postImage;
    private String content;
}