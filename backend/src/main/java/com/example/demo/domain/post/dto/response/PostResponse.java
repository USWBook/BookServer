package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String postName,
        Integer postPrice,
        String postImage,
        String content,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getPostName(),
                post.getPostPrice(),
                post.getPostImage(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
