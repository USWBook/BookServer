package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.entity.Post;
import com.example.demo.domain.user.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        @Schema(description = "사진 경로 리스트", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        List<String> postImages,
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
        @Schema(description = "판매자 닉네임",example = "박스프링")
        String sellerName,
        @Schema(description = "판매자 식별값",example = "UUID")
        UUID sellerId,
        @Schema(description = "댓글들")
        List<CommentResponse> comments
) {
    public static PostResponse from(Post post, List<String> presignedUrls) {
        List<CommentResponse> commentResponses = post.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        String author = switch (post.getSeller().getStatus()) {
            case WITHDRAWAL -> "탈퇴한 사용자";
            case BANNED -> "밴 당한 사용자";
            default -> post.getSeller().getName();
        };
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getPostName(),
                post.getPostPrice(),
                post.getCourseName(),
                post.getProfessor(),
                presignedUrls != null ? presignedUrls : new ArrayList<>(),
                post.getContent(),
                post.getCreatedAt(),
                post.getLikeCount(),
                post.getMajor().getName(),
                post.getStatus().getValue(),
                author,
                post.getSeller().getId(),
                commentResponses
        );
    }
}
