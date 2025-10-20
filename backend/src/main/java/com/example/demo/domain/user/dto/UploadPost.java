package com.example.demo.domain.user.dto;

import com.example.demo.domain.post.entity.Post;

import java.time.LocalDateTime;

public record UploadPost(
        String postImage,
        String postTitle,
        String status,
        Integer price,
        LocalDateTime createdAt,
        Integer likeCount,
        String content
) {
    public static UploadPost from(Post post) {
        String statusValue = (post.getStatus() != null) ? post.getStatus().getValue() : null;

        return new UploadPost(
                post.getPostImage(),
                post.getTitle(),
                statusValue,
                post.getPostPrice(),
                post.getCreatedAt(),
                post.getLikeCount(),
                post.getContent()
        );
    }
}
