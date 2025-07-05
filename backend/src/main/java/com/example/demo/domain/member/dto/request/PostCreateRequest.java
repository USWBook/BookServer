package com.example.demo.domain.member.dto.request;

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