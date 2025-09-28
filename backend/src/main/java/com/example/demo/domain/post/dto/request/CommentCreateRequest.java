package com.example.demo.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        String content
) {
}
