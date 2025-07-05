package com.example.demo.domain.post.dto.request;

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