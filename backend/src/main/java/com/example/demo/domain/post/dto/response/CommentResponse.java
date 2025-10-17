package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.PostComment;
import com.example.demo.domain.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "댓글 정보 응답 DTO") // 👈 클래스 레벨 @Schema 추가
public record CommentResponse(
        @Schema(description = "댓글 ID")
        UUID commentId,
        @Schema(description = "작성자 ID")
        UUID authorId,
        @Schema(description = "댓글 내용", example = "좋은 책이네요!")
        String content,
        @Schema(description = "작성자 이름", example = "김스프링")
        String authorName,
        @Schema(description = "작성일")
        LocalDateTime createdAt,
        @Schema(description = "수정일")
        LocalDateTime modifiedAt
) {
    public static CommentResponse from(PostComment comment) {
        String author = switch (comment.getUser().getStatus()) {
            case WITHDRAWAL -> "탈퇴한 사용자";
            case BANNED -> "밴 당한 사용자";
            default -> comment.getUser().getName();
        };
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(), // 만약 댓글에서 바로 채팅으로 넘어간다면 필요함.
                comment.getContent(),
                author, // 닉네임 받아옴
                comment.getCreatedAt(),
                comment.getModifiedAt()
        );
    }
}
