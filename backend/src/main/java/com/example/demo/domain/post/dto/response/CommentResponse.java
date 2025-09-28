package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.PostComment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID commentId,
        UUID authorId,
        String content,
        String authorName, //작성자 이름
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static CommentResponse from(PostComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(), // 만약 댓글에서 바로 채팅으로 넘어간다면 필요함.
                comment.getContent(),
                comment.getUser().getName(), // 닉네임 받아옴
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
