package com.example.demo.domain.post.dto.response;

import com.example.demo.domain.post.enums.PostStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;
// 제목,대표사진,가격,찜 개수,업로드 시간,학년,전공
// 제목,대표사진,가격,찜 개수,업로드 시간,학년,전공
public record PostListResponse(
        UUID id,
        String title,
        Integer postPrice,
        Integer likeCount,
        Long commentCount,
        Integer grade,
        Integer semester,
        String status,
        LocalDateTime createdAt
) {

    public PostListResponse(UUID id, String title, Integer postPrice, Integer likeCount, Long commentCount, Integer grade, Integer semester, PostStatus status, LocalDateTime createdAt) {
        this(id, title, postPrice, likeCount, commentCount, grade, semester, status.name(), createdAt);
    }
}