package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "게시글 상세 정보 응답 DTO")
public record PostResponse(
        @Schema(description = "게시글 ID", example = "a1b2c3d4-...")
        UUID id,
        @Schema(description = "게시물 제목", example = "자료구조 싸게 팝니다")
        String title,
        @Schema(description = "책이름", example = "컴퓨터와 자료구조")
        String postName,
        @Schema(description = "가격", example = "10000")
        Integer postPrice,
        @Schema(description = "강의명", example = "컴공 자료구조강의")
        String courseName,
        @Schema(description = "교수명", example = "홍길동 교슈")
        String professorName,
        @Schema(description = "사진 경로", example = "https://example.com/image.jpg")
        String postImage,
        @Schema(description = "게시물 본문", example = "졸업해서 싸게 팝니다")
        String content,
        @Schema(description = "작성일", example = "yyyy-mm-dd")
        LocalDateTime createdAt,
        @Schema(description = "찜개수", example = "2")
        Integer likeCount,
        @Schema(description = "전공명", example = "컴퓨터 공학과")
        String majorName,
        @Schema(description = "판매상태", example = "판매중")
        String PostStatus,
        @Schema(description = "댓글들")
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
                post.getCourseName(),
                post.getProfessor(),
                post.getPostImage(),
                post.getContent(),
                post.getCreatedAt(),
                post.getLikeCount(),
                post.getMajor().getName(),
                post.getStatus().getValue(),
                commentResponses
        );
    }
}
