package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.enums.PostStatus;
import com.example.demo.domain.user.entity.Grade;
import com.example.demo.domain.user.entity.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;
// 제목,대표사진,가격,찜 개수,업로드 시간,학년,전공
//@Schema(description = "게시글 목록 응답 DTO")
public record PostListResponse(
        //@Schema(description = "게시글 UUID", example = "550e8400-...")
        UUID id,

        //@Schema(description = "게시글 제목", example = "중고 노트북 판매합니다")
        String title,

        //@Schema(description = "가격", example = "500000")
        Integer postPrice,

        //@Schema(description = "찜 개수", example = "10")
        Integer likeCount,

        //@Schema(description = "댓글 개수", example = "3")
        Long commentCount,

        //@Schema(description = "학년", example = "2")
        Integer grade,

        //@Schema(description = "학기", example = "1")
        Integer semester,

        //@Schema(description = "게시글 상태", example = "ACTIVE")
        String status,

        //@Schema(description = "업로드 시간", example = "2025-10-10T12:34:56")
        LocalDateTime createdAt
) {

    public PostListResponse(UUID id, String title, Integer postPrice, Integer likeCount, Long commentCount, Grade grade, Semester semester, PostStatus status, LocalDateTime createdAt) {
        this(
                id,
                title,
                postPrice,
                likeCount,
                commentCount,
                grade != null ? grade.getValue() : null,      // Enum을 숫자로 변환
                semester != null ? semester.getValue() : null, // Enum을 숫자로 변환
                status != null ? status.name() : null,
                createdAt
        );
    }
}