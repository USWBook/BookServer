package com.example.demo.domain.user.dto;

import com.example.demo.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadPost(
        String postImage,
        String postTitle,
        String status,
        Integer price,
        LocalDateTime createdAt,
        Integer likeCount,
        String content,
        UUID postId
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
                post.getContent(),
                post.getId()
        );
    }
}
