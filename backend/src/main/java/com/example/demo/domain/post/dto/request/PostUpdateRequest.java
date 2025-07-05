package com.example.demo.domain.post.dto.request;

public record PostUpdateRequest(
        String title,
        String content,
        Integer postPrice
) {
}
