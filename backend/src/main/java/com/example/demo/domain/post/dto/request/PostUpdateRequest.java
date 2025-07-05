package com.example.demo.domain.post.dto.request;


import lombok.Getter;

@Getter
public class PostUpdateRequest {
    private String title;
    private String content;
    private Integer postPrice;
}
