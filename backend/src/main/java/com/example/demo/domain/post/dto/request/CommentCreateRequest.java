package com.example.demo.domain.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "댓글 생성 요청 DTO")
@Builder
public record CommentCreateRequest(
        @Schema(description = "댓글 내용", example = "책 상태 괜찮나요?")
        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        String content
) {
}
