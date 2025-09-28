package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String title,
        String postName,
        Integer postPrice,
        String postImage,
        String content,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {
    public static PostResponse from(Post post) {
        // 댓글이 없으면 빈 배열
        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getPostName(),
                post.getPostPrice(),
                post.getPostImage(),
                post.getContent(),
                post.getCreatedAt(),
                commentResponses
        );
    }
}
